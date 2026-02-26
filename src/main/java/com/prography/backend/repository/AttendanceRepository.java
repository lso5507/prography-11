package com.prography.backend.repository;

import com.prography.backend.entity.Attendance;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.SessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByCohortMemberAndSession(CohortMember cohortMember, SessionEntity session);

    List<Attendance> findByCohortMemberOrderByCreatedAtDesc(CohortMember cohortMember);

    List<Attendance> findBySessionOrderByCreatedAtDesc(SessionEntity session);

    List<Attendance> findByCohortMember_Member_IdOrderByCreatedAtDesc(Long memberId);
}
