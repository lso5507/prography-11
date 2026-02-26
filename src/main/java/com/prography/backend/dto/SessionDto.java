package com.prography.backend.dto;

import com.prography.backend.domain.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SessionDto {

    public record SessionResponse(
        Long id,
        String title,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        SessionStatus status,
        Long qrCodeId,
        String qrHashValue,
        LocalDateTime qrExpiresAt
    ) {
    }

    public record CreateSessionRequest(
        @NotBlank(message = "title은 필수입니다") String title,
        @NotNull(message = "sessionDate는 필수입니다") LocalDate sessionDate,
        @NotNull(message = "startTime은 필수입니다") LocalTime startTime,
        @NotNull(message = "endTime은 필수입니다") LocalTime endTime,
        @NotNull(message = "status는 필수입니다") SessionStatus status
    ) {
    }

    public record UpdateSessionRequest(
        @NotBlank(message = "title은 필수입니다") String title,
        @NotNull(message = "sessionDate는 필수입니다") LocalDate sessionDate,
        @NotNull(message = "startTime은 필수입니다") LocalTime startTime,
        @NotNull(message = "endTime은 필수입니다") LocalTime endTime,
        @NotNull(message = "status는 필수입니다") SessionStatus status
    ) {
    }

    public record QrCodeResponse(Long id, Long sessionId, String hashValue, LocalDateTime expiresAt, boolean active) {
    }
}
