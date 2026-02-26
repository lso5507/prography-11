package com.prography.backend.repository;

import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.DepositHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {
    List<DepositHistory> findByCohortMemberOrderByCreatedAtDesc(CohortMember cohortMember);
}
