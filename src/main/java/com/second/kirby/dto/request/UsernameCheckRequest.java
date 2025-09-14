package com.second.kirby.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "아이디 중복 확인 요청")
public record UsernameCheckRequest(
        @Schema(description = "아이디", example = "user123")
        @NotBlank(message = "아이디를 입력해주세요")
        @Size(min = 4, max = 50, message = "아이디는 4-50자 사이로 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용 가능합니다")
        String username
) {
}