package com.prography.backend.service;

import com.prography.backend.domain.SessionStatus;
import com.prography.backend.dto.SessionDto;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.SessionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CohortResolver cohortResolver;
    private final QrCodeService qrCodeService;

    public SessionService(SessionRepository sessionRepository, CohortResolver cohortResolver, QrCodeService qrCodeService) {
        this.sessionRepository = sessionRepository;
        this.cohortResolver = cohortResolver;
        this.qrCodeService = qrCodeService;
    }

    @Transactional(readOnly = true)
    public List<SessionDto.SessionResponse> getMemberSessions() {
        Cohort current = cohortResolver.getCurrentCohort();
        return sessionRepository.findByCohortAndStatusNotOrderBySessionDateDescStartTimeDesc(current, SessionStatus.CANCELLED)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionDto.SessionResponse> getAdminSessions() {
        Cohort current = cohortResolver.getCurrentCohort();
        return sessionRepository.findByCohortOrderBySessionDateDescStartTimeDesc(current)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public SessionDto.SessionResponse create(SessionDto.CreateSessionRequest request) {
        Cohort current = cohortResolver.getCurrentCohort();
        SessionEntity session = new SessionEntity(current, request.title(), request.sessionDate(), request.startTime(), request.endTime(),
            request.status());
        SessionEntity saved = sessionRepository.save(session);
        qrCodeService.createForSession(saved);
        return toDto(saved);
    }

    @Transactional
    public SessionDto.SessionResponse update(Long sessionId, SessionDto.UpdateSessionRequest request) {
        SessionEntity session = findSession(sessionId);
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new AppException(ErrorCode.SESSION_ALREADY_CANCELLED);
        }
        session.update(request.title(), request.sessionDate(), request.startTime(), request.endTime(), request.status());
        return toDto(session);
    }

    @Transactional
    public void delete(Long sessionId) {
        SessionEntity session = findSession(sessionId);
        session.cancel();
    }

    @Transactional(readOnly = true)
    public SessionEntity findSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
    }

    private SessionDto.SessionResponse toDto(SessionEntity session) {
        QrCode qrCode = qrCodeService.getActiveBySession(session);
        return new SessionDto.SessionResponse(
            session.getId(),
            session.getTitle(),
            session.getSessionDate(),
            session.getStartTime(),
            session.getEndTime(),
            session.getStatus(),
            qrCode == null ? null : qrCode.getId(),
            qrCode == null ? null : qrCode.getHashValue(),
            qrCode == null ? null : qrCode.getExpiresAt()
        );
    }
}
