package com.prography.backend.repository;

import com.prography.backend.domain.SessionStatus;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.SessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SessionEntity> findByCohortOrderBySessionDateDescStartTimeDesc(Cohort cohort);

    List<SessionEntity> findByCohortAndStatusNotOrderBySessionDateDescStartTimeDesc(Cohort cohort, SessionStatus status);

    Optional<SessionEntity> findFirstByTitle(String title);
}
