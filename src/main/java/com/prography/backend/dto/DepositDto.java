package com.prography.backend.dto;

import com.prography.backend.domain.DepositType;
import java.time.LocalDateTime;

public class DepositDto {

    public record DepositHistoryResponse(
        Long id,
        DepositType type,
        int amount,
        int balanceAfter,
        String reason,
        LocalDateTime createdAt
    ) {
    }
}
