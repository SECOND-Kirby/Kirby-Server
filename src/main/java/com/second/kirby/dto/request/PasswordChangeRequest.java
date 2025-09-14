package com.second.kirby.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청")
public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "oldPassword123!")
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newPassword123!")
        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이로 입력해주세요")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "비밀번호는 영문자, 숫자, 특수문자를 모두 포함해야 합니다")
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "newPassword123!")
        @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
        String newPasswordConfirm
) {
}