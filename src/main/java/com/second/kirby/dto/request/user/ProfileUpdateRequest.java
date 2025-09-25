package com.second.kirby.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "프로필 정보 변경 요청")
public record ProfileUpdateRequest(
        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요")
        @Size(min = 2, max = 10, message = "이름은 2-10자 사이로 입력해주세요")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영문자만 사용 가능합니다")
        String name,

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다")
        String email,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식으로 입력해주세요")
        String phoneNumber
) {
}