package com.prography.backend.repository;

import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortMemberRepository extends JpaRepository<CohortMember, Long> {
    Optional<CohortMember> findByMemberAndCohort(Member member, Cohort cohort);

    List<CohortMember> findByCohort(Cohort cohort);

    List<CohortMember> findByMember(Member member);
}
