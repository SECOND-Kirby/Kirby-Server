package com.second.kirby.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "훈련 설정 요청")
public record TrainingConfigRequest(
        @NotNull(message = "강도를 선택해주세요.")
        @Min(value = 1, message = "강도는 1-10 사이여야 합니다.")
        @Max(value = 10, message = "강도는 1-10 사이여야 합니다.")
        Integer intensity,

        @NotNull(message = "방향을 선택해주세요.")
        @Min(value = 1, message = "방향은 1-10 사이여야 합니다.")
        @Max(value = 10, message = "방향은 1-10 사이여야 합니다.")
        Integer direction,

        @NotNull(message = "빈도를 선택해주세요.")
        @Min(value = 1, message = "빈도는 1-10 사이여야 합니다.")
        @Max(value = 10, message = "빈도는 1-10 사이여야 합니다.")
        Integer frequency,

        @NotNull(message = "시간을 선택해주세요.")
        @Min(value = 15, message = "훈련 시간은 15분 이상이어야 합니다.")
        @Max(value = 120, message = "훈련 시간은 120분 이하여야 합니다.")
        Integer durationMinutes
) {
}