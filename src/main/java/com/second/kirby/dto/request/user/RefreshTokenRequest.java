package com.second.kirby.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 갱신 요청")
public record RefreshTokenRequest(
        @NotBlank(message = "리프레시 토큰이 필요합니다.")
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {}
