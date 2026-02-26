package com.prography.backend.unit.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.prography.backend.domain.AttendanceSource;
import com.prography.backend.domain.AttendanceStatus;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.entity.Attendance;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.entity.Team;
import com.prography.backend.repository.AttendanceRepository;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.service.AttendanceService;
import com.prography.backend.service.CohortResolver;
import com.prography.backend.service.DepositService;
import com.prography.backend.service.MemberService;
import com.prography.backend.service.QrCodeService;
import com.prography.backend.service.SessionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private CohortMemberRepository cohortMemberRepository;
    @Mock private MemberService memberService;
    @Mock private SessionService sessionService;
    @Mock private QrCodeService qrCodeService;
    @Mock private CohortResolver cohortResolver;
    @Mock private DepositService depositService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    @DisplayName("일정별 출결 요약 집계")
    void getSessionSummary_aggregates() {
        Cohort cohort = new Cohort("11기", true);
        SessionEntity session = new SessionEntity(cohort, "세션", LocalDate.now(), LocalTime.NOON, LocalTime.MAX,
            com.prography.backend.domain.SessionStatus.IN_PROGRESS);
        when(sessionService.findSession(1L)).thenReturn(session);

        Attendance a1 = new Attendance(createCm(), session, AttendanceStatus.PRESENT, 0, 0, AttendanceSource.MANUAL,
            LocalDateTime.now());
        Attendance a2 = new Attendance(createCm(), session, AttendanceStatus.LATE, 5, 2500, AttendanceSource.QR,
            LocalDateTime.now());
        when(attendanceRepository.findBySessionOrderByCreatedAtDesc(session)).thenReturn(List.of(a1, a2));

        AttendanceDto.SessionAttendanceSummaryResponse summary = attendanceService.getSessionSummary(1L);

        assertEquals(1, summary.present());
        assertEquals(1, summary.late());
        assertEquals(2500, summary.totalPenalty());
    }

    private CohortMember createCm() {
        Cohort cohort = new Cohort("11기", true);
        Member member = new Member("u1", "pw", "회원", com.prography.backend.domain.MemberRole.MEMBER,
            com.prography.backend.domain.MemberStatus.ACTIVE);
        Part part = new Part(cohort, "SERVER");
        Team team = new Team(cohort, "Team A");
        return new CohortMember(cohort, member, part, team, 100000);
    }
}
