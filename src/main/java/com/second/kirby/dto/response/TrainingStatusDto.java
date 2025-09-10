package com.second.kirby.dto.response;

import com.second.kirby.domain.Training;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "훈련 상태 정보")
public record TrainingStatusDto(
        @Schema(description = "훈련 ID")
        Long trainingId,

        @Schema(description = "훈련 상태")
        String status,

        @Schema(description = "시작 시간")
        LocalDateTime startedAt,

        @Schema(description = "일시정지 시간")
        LocalDateTime pausedAt,

        @Schema(description = "완료 시간")
        LocalDateTime completedAt,

        @Schema(description = "경과 시간(초)")
        Long elapsedSeconds,

        @Schema(description = "진행률(%)")
        Double progressPercentage,

        @Schema(description = "훈련 설정")
        TrainingConfig config
) {
    public static TrainingStatusDto from(Training training) {
        long elapsedSeconds = training.getActualTrainingSeconds();
        long totalSeconds = training.getDurationMinutes() * 60L;
        double progressPercentage = totalSeconds > 0 ? (double) elapsedSeconds / totalSeconds * 100 : 0;

        return new TrainingStatusDto(
                training.getId(),
                training.getStatus().name(),
                training.getStartedAt(),
                training.getPausedAt(),
                training.getCompletedAt(),
                elapsedSeconds,
                Math.min(100.0, progressPercentage),
                new TrainingConfig(
                        training.getIntensity(),
                        training.getDirection(),
                        training.getFrequency(),
                        training.getDurationMinutes()
                )
        );
    }

    @Schema(description = "훈련 설정")
    public record TrainingConfig(
            @Schema(description = "강도")
            Integer intensity,
            @Schema(description = "방향")
            Integer direction,
            @Schema(description = "빈도")
            Integer frequency,
            @Schema(description = "시간(분)")
            Integer durationMinutes
    ) {}
}