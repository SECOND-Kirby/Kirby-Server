package com.second.kirby.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그아웃 요청")
public record LogoutRequest(
        @Schema(description = "액세스 토큰 (선택사항, Authorization 헤더 우선)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "리프레시 토큰 (선택사항)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {
}