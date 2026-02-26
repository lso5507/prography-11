package com.prography.backend.integration.cohort;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prography.backend.entity.Cohort;
import com.prography.backend.repository.CohortRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CohortControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CohortRepository cohortRepository;

    @Test
    @DisplayName("Cohort 도메인 통합 - 기수 목록/상세 조회")
    void get_cohorts_and_detail() throws Exception {
        Cohort cohort11 = cohortRepository.findByName("11기").orElseThrow();

        mockMvc.perform(get("/api/v1/admin/cohorts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/v1/admin/cohorts/{id}", cohort11.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("11기"));
    }
}
