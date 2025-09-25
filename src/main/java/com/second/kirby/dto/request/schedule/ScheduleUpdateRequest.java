package com.second.kirby.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "스케줄 수정 요청")
public record ScheduleUpdateRequest(
        @Schema(description = "스케줄 제목", example = "테니스 동작 반복 훈련 (수정)")
        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
        String title,

        @Schema(description = "스케줄 날짜", example = "2025-10-14")
        @NotNull(message = "날짜를 입력해주세요")
        LocalDate scheduleDate,

        @Schema(description = "시작 시간", example = "18:00:00")
        @NotNull(message = "시작 시간을 입력해주세요")
        LocalTime startTime,

        @Schema(description = "종료 시간", example = "20:00:00")
        @NotNull(message = "종료 시간을 입력해주세요")
        LocalTime endTime,

        @Schema(description = "메모", example = "집중 동작 반복 (수정)")
        @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
        String memo
) {
}