package com.second.kirby.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "스케줄 생성 요청")
public record ScheduleCreateRequest(
        @Schema(description = "스케줄 제목", example = "테니스 동작 반복 훈련")
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

        @Schema(description = "메모", example = "집중 동작 반복")
        @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
        String memo,

        @Schema(description = "반복 요일 (매주 반복)", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
        List<DayOfWeek> repeatDays,

        @Schema(description = "반복 종료 날짜 (반복 설정 시 필수)", example = "2025-12-31")
        LocalDate repeatEndDate
) {
        /**
         * 반복 일정 검증
         * 반복 요일이 설정된 경우 종료 날짜가 필수입니다.
         */
        public void validate() {
                if (repeatDays != null && !repeatDays.isEmpty()) {
                        if (repeatEndDate == null) {
                                throw new IllegalArgumentException("반복 종료 날짜는 필수입니다.");
                        }
                        if (repeatEndDate.isBefore(scheduleDate)) {
                                throw new IllegalArgumentException("종료 날짜는 시작 날짜 이후여야 합니다.");
                        }
                }
        }
}