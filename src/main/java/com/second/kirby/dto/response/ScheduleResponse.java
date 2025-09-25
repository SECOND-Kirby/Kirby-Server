package com.second.kirby.dto.response;

import com.second.kirby.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "스케줄 응답")
public record ScheduleResponse(
        @Schema(description = "스케줄 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "테니스 동작 반복 훈련")
        String title,

        @Schema(description = "스케줄 날짜", example = "2025-10-14")
        LocalDate scheduleDate,

        @Schema(description = "시작 시간", example = "18:00:00")
        LocalTime startTime,

        @Schema(description = "종료 시간", example = "20:00:00")
        LocalTime endTime,

        @Schema(description = "메모", example = "집중 동작 반복")
        String memo
) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getScheduleDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getMemo()
        );
    }
}