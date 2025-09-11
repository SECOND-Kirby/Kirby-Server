package com.second.kirby.dto.response;

import com.second.kirby.domain.Training;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "훈련 시작 응답")
public record TrainingStartResponse(
        @Schema(description = "훈련 ID")
        Long trainingId,

        @Schema(description = "훈련 상태")
        String status,

        @Schema(description = "시작 시간")
        LocalDateTime startedAt,

        @Schema(description = "예상 종료 시간")
        LocalDateTime expectedEndTime
) {
    public static TrainingStartResponse of(Training training) {
        LocalDateTime expectedEndTime = training.getStartedAt() != null
                ? training.getStartedAt().plusMinutes(training.getDurationMinutes())
                : null;

        return new TrainingStartResponse(
                training.getId(),
                training.getStatus().name(),
                training.getStartedAt(),
                expectedEndTime
        );
    }
}