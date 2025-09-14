package com.second.kirby.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "아이디", example = "user123")
        @NotBlank(message = "아이디를 입력해주세요")
        @Size(min = 6, max = 20, message = "아이디는 6-20자 사이로 입력해주세요")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용 가능합니다")
        String username,

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Size(max = 50, message = "이메일은 50자를 초과할 수 없습니다")
        String email,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @NotBlank(message = "전화번호를 입력해주세요")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식으로 입력해주세요")
        String phoneNumber,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름을 입력해주세요")
        @Size(min = 2, max = 10, message = "이름은 2-10자 사이로 입력해주세요")
        @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영문자만 사용 가능합니다")
        String name,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이로 입력해주세요")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다")
        String password,

        @Schema(description = "비밀번호 확인", example = "password123!")
        @NotBlank(message = "비밀번호 확인을 입력해주세요")
        String passwordConfirm
) {
}