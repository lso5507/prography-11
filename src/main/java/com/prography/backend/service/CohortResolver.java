package com.prography.backend.service;

import com.prography.backend.entity.Cohort;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.CohortRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CohortResolver {

    private final CohortRepository cohortRepository;
    private final String currentCohortName;

    public CohortResolver(CohortRepository cohortRepository,
                          @Value("${app.current-cohort-name:11기}") String currentCohortName) {
        this.cohortRepository = cohortRepository;
        this.currentCohortName = currentCohortName;
    }

    public Cohort getCurrentCohort() {
        return cohortRepository.findByName(currentCohortName)
            .or(() -> cohortRepository.findByCurrentTrue())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_NOT_FOUND));
    }
}
