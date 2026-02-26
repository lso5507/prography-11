package com.prography.backend.repository;

import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.Part;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByCohort(Cohort cohort);

    Optional<Part> findByIdAndCohort(Long id, Cohort cohort);

    Optional<Part> findByCohortAndName(Cohort cohort, String name);
}
