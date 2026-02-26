package com.prography.backend.service;

import com.prography.backend.domain.AttendanceSource;
import com.prography.backend.domain.AttendanceStatus;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.domain.SessionStatus;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.entity.Attendance;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.AttendanceRepository;
import com.prography.backend.repository.CohortMemberRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final MemberService memberService;
    private final SessionService sessionService;
    private final QrCodeService qrCodeService;
    private final CohortResolver cohortResolver;
    private final DepositService depositService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             CohortMemberRepository cohortMemberRepository,
                             MemberService memberService,
                             SessionService sessionService,
                             QrCodeService qrCodeService,
                             CohortResolver cohortResolver,
                             DepositService depositService) {
        this.attendanceRepository = attendanceRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.memberService = memberService;
        this.sessionService = sessionService;
        this.qrCodeService = qrCodeService;
        this.cohortResolver = cohortResolver;
        this.depositService = depositService;
    }

    @Transactional
    public AttendanceDto.AttendanceResponse checkIn(AttendanceDto.CheckInRequest request) {
        QrCode qrCode = qrCodeService.findByHash(request.qrHashValue());
        if (!qrCode.isActive()) {
            throw new AppException(ErrorCode.QR_INVALID);
        }
        if (qrCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.QR_EXPIRED);
        }

        SessionEntity session = qrCode.getSession();
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }

        Member member = memberService.findMember(request.memberId());
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.MEMBER_WITHDRAWN);
        }

        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, session.getCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        attendanceRepository.findByCohortMemberAndSession(cohortMember, session)
            .ifPresent(it -> {
                throw new AppException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
            });

        LocalDateTime sessionStartAt = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status;
        int lateMinutes;
        if (now.isAfter(sessionStartAt)) {
            status = AttendanceStatus.LATE;
            lateMinutes = (int) Duration.between(sessionStartAt, now).toMinutes();
        } else {
            status = AttendanceStatus.PRESENT;
            lateMinutes = 0;
        }

        int penalty = calculatePenalty(status, lateMinutes);
        if (penalty > 0) {
            depositService.applyPenalty(cohortMember, penalty, "QR_CHECK_IN");
        }

        Attendance attendance = attendanceRepository.save(
            new Attendance(cohortMember, session, status, lateMinutes, penalty, AttendanceSource.QR, LocalDateTime.now())
        );
        return toDto(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.AttendanceResponse> getMyAttendances(Long memberId) {
        Member member = memberService.findMember(memberId);
        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, cohortResolver.getCurrentCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        return attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceDto.AttendanceSummaryResponse getMySummary(Long memberId) {
        Member member = memberService.findMember(memberId);
        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, cohortResolver.getCurrentCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        List<Attendance> attendances = attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember);
        return toSummary(attendances, cohortMember.getDepositBalance());
    }

    @Transactional
    public AttendanceDto.AttendanceResponse register(AttendanceDto.RegisterAttendanceRequest request) {
        SessionEntity session = sessionService.findSession(request.sessionId());
        Member member = memberService.findMember(request.memberId());
        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, session.getCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        attendanceRepository.findByCohortMemberAndSession(cohortMember, session)
            .ifPresent(it -> {
                throw new AppException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
            });

        validateExcusedLimitForCreate(cohortMember, request.status());
        int penalty = calculatePenalty(request.status(), request.lateMinutes());
        if (penalty > 0) {
            depositService.applyPenalty(cohortMember, penalty, "ADMIN_REGISTER");
        }
        if (request.status() == AttendanceStatus.EXCUSED) {
            cohortMember.increaseExcusedCount();
        }

        Attendance attendance = attendanceRepository.save(
            new Attendance(cohortMember, session, request.status(), request.lateMinutes(), penalty, AttendanceSource.MANUAL,
                LocalDateTime.now())
        );
        return toDto(attendance);
    }

    @Transactional
    public AttendanceDto.AttendanceResponse update(Long attendanceId, AttendanceDto.UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_NOT_FOUND));
        CohortMember cohortMember = attendance.getCohortMember();

        AttendanceStatus oldStatus = attendance.getStatus();
        int oldPenalty = attendance.getPenaltyAmount();
        int newPenalty = calculatePenalty(request.status(), request.lateMinutes());

        if (oldStatus != AttendanceStatus.EXCUSED && request.status() == AttendanceStatus.EXCUSED) {
            if (cohortMember.getExcusedCount() >= 3) {
                throw new AppException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
            }
            cohortMember.increaseExcusedCount();
        } else if (oldStatus == AttendanceStatus.EXCUSED && request.status() != AttendanceStatus.EXCUSED) {
            cohortMember.decreaseExcusedCount();
        }

        if (newPenalty > oldPenalty) {
            depositService.applyPenalty(cohortMember, newPenalty - oldPenalty, "ATTENDANCE_UPDATE");
        } else if (newPenalty < oldPenalty) {
            depositService.refund(cohortMember, oldPenalty - newPenalty, "ATTENDANCE_UPDATE");
        }

        attendance.update(request.status(), request.lateMinutes(), newPenalty);
        return toDto(attendance);
    }

    @Transactional(readOnly = true)
    public AttendanceDto.SessionAttendanceSummaryResponse getSessionSummary(Long sessionId) {
        SessionEntity session = sessionService.findSession(sessionId);
        List<Attendance> attendances = attendanceRepository.findBySessionOrderByCreatedAtDesc(session);
        long present = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long late = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absent = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long excused = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();
        int totalPenalty = attendances.stream().mapToInt(Attendance::getPenaltyAmount).sum();
        return new AttendanceDto.SessionAttendanceSummaryResponse(present, late, absent, excused, totalPenalty);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.AttendanceResponse> getMemberAttendances(Long memberId) {
        return attendanceRepository.findByCohortMember_Member_IdOrderByCreatedAtDesc(memberId)
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.AttendanceResponse> getSessionAttendances(Long sessionId) {
        SessionEntity session = sessionService.findSession(sessionId);
        return attendanceRepository.findBySessionOrderByCreatedAtDesc(session).stream().map(this::toDto).toList();
    }

    private void validateExcusedLimitForCreate(CohortMember cohortMember, AttendanceStatus status) {
        if (status == AttendanceStatus.EXCUSED && cohortMember.getExcusedCount() >= 3) {
            throw new AppException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
        }
    }

    private int calculatePenalty(AttendanceStatus status, int lateMinutes) {
        return switch (status) {
            case PRESENT, EXCUSED -> 0;
            case ABSENT -> 10_000;
            case LATE -> Math.min(Math.max(lateMinutes, 0) * 500, 10_000);
        };
    }

    private AttendanceDto.AttendanceResponse toDto(Attendance attendance) {
        return new AttendanceDto.AttendanceResponse(
            attendance.getId(),
            attendance.getSession().getId(),
            attendance.getSession().getTitle(),
            attendance.getCohortMember().getMember().getId(),
            attendance.getCohortMember().getMember().getName(),
            attendance.getStatus(),
            attendance.getLateMinutes(),
            attendance.getPenaltyAmount(),
            attendance.getCheckedAt()
        );
    }

    private AttendanceDto.AttendanceSummaryResponse toSummary(List<Attendance> attendances, int currentDeposit) {
        long present = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long late = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absent = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long excused = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();
        int totalPenalty = attendances.stream().mapToInt(Attendance::getPenaltyAmount).sum();
        return new AttendanceDto.AttendanceSummaryResponse(present, late, absent, excused, totalPenalty, currentDeposit);
    }
}
