package com.prography.backend.service;

import com.prography.backend.domain.DepositType;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.DepositHistory;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.DepositHistoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositService {

    private final DepositHistoryRepository depositHistoryRepository;

    public DepositService(DepositHistoryRepository depositHistoryRepository) {
        this.depositHistoryRepository = depositHistoryRepository;
    }

    @Transactional
    public void recordInitial(CohortMember cohortMember, int amount) {
        depositHistoryRepository.save(new DepositHistory(cohortMember, DepositType.INITIAL, amount,
            cohortMember.getDepositBalance(), "INITIAL_DEPOSIT"));
    }

    @Transactional
    public void applyPenalty(CohortMember cohortMember, int amount, String reason) {
        if (amount <= 0) {
            return;
        }
        if (cohortMember.getDepositBalance() < amount) {
            throw new AppException(ErrorCode.DEPOSIT_INSUFFICIENT);
        }
        cohortMember.withdrawPenalty(amount);
        depositHistoryRepository.save(new DepositHistory(cohortMember, DepositType.PENALTY, amount,
            cohortMember.getDepositBalance(), reason));
    }

    @Transactional
    public void refund(CohortMember cohortMember, int amount, String reason) {
        if (amount <= 0) {
            return;
        }
        cohortMember.refund(amount);
        depositHistoryRepository.save(new DepositHistory(cohortMember, DepositType.REFUND, amount,
            cohortMember.getDepositBalance(), reason));
    }

    public List<DepositHistory> getHistories(CohortMember cohortMember) {
        return depositHistoryRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember);
    }
}
