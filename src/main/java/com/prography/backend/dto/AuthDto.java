package com.prography.backend.dto;

import com.prography.backend.domain.MemberRole;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

    public record LoginRequest(
        @NotBlank(message = "loginId는 필수입니다") String loginId,
        @NotBlank(message = "password는 필수입니다") String password
    ) {
    }

    public record LoginResponse(Long memberId, String loginId, String name, MemberRole role) {
    }
}
