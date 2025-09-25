package com.second.kirby.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "아이디", example = "user123")
        @NotBlank(message = "아이디를 입력해주세요")
        @Size(max = 50, message = "아이디는 50자를 초과할 수 없습니다")
        String username,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(max = 20, message = "비밀번호는 20자를 초과할 수 없습니다")
        String password
) {
}