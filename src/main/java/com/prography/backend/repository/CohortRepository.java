package com.prography.backend.repository;

import com.prography.backend.entity.Cohort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortRepository extends JpaRepository<Cohort, Long> {
    Optional<Cohort> findByName(String name);

    Optional<Cohort> findByCurrentTrue();
}
