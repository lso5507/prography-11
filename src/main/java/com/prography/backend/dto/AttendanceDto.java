package com.prography.backend.dto;

import com.prography.backend.domain.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AttendanceDto {

    public record CheckInRequest(
        @NotBlank(message = "qrHashValue는 필수입니다") String qrHashValue,
        @NotNull(message = "memberId는 필수입니다") Long memberId
    ) {
    }

    public record RegisterAttendanceRequest(
        @NotNull(message = "sessionId는 필수입니다") Long sessionId,
        @NotNull(message = "memberId는 필수입니다") Long memberId,
        @NotNull(message = "status는 필수입니다") AttendanceStatus status,
        int lateMinutes
    ) {
    }

    public record UpdateAttendanceRequest(
        @NotNull(message = "status는 필수입니다") AttendanceStatus status,
        int lateMinutes
    ) {
    }

    public record AttendanceResponse(
        Long id,
        Long sessionId,
        String sessionTitle,
        Long memberId,
        String memberName,
        AttendanceStatus status,
        int lateMinutes,
        int penaltyAmount,
        LocalDateTime checkedAt
    ) {
    }

    public record AttendanceSummaryResponse(
        long present,
        long late,
        long absent,
        long excused,
        int totalPenalty,
        int currentDeposit
    ) {
    }

    public record SessionAttendanceSummaryResponse(
        long present,
        long late,
        long absent,
        long excused,
        int totalPenalty
    ) {
    }
}
