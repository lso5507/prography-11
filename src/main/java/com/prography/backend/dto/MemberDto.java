package com.prography.backend.dto;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MemberDto {

    public record MemberSimpleResponse(Long id, String loginId, String name, MemberRole role, MemberStatus status) {
    }

    public record MemberDetailResponse(
        Long id,
        String loginId,
        String name,
        MemberRole role,
        MemberStatus status,
        Long cohortMemberId,
        String cohortName,
        String partName,
        String teamName,
        int depositBalance,
        int excusedCount
    ) {
    }

    public record CreateMemberRequest(
        @NotBlank(message = "loginId는 필수입니다") String loginId,
        @NotBlank(message = "password는 필수입니다") String password,
        @NotBlank(message = "name은 필수입니다") String name,
        @NotNull(message = "role은 필수입니다") MemberRole role,
        @NotNull(message = "cohortId는 필수입니다") Long cohortId,
        @NotNull(message = "partId는 필수입니다") Long partId,
        Long teamId
    ) {
    }

    public record UpdateMemberRequest(
        @NotBlank(message = "name은 필수입니다") String name,
        @NotNull(message = "role은 필수입니다") MemberRole role,
        @NotNull(message = "status는 필수입니다") MemberStatus status,
        @NotNull(message = "partId는 필수입니다") Long partId,
        Long teamId
    ) {
    }
}
