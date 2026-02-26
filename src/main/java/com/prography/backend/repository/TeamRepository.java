package com.prography.backend.repository;

import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByCohort(Cohort cohort);

    Optional<Team> findByIdAndCohort(Long id, Cohort cohort);

    Optional<Team> findByCohortAndName(Cohort cohort, String name);
}
