package com.second.kirby.controller;

import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.request.schedule.ScheduleCreateRequest;
import com.second.kirby.dto.request.schedule.ScheduleUpdateRequest;
import com.second.kirby.dto.response.ScheduleResponse;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.service.ScheduleService;
import com.second.kirby.service.ScheduleService.DeleteScope;
import com.second.kirby.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "스케줄", description = "스케줄 관리 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "스케줄 생성", description = "새로운 스케줄을 생성합니다. 반복 설정 시 자동으로 반복 일정이 생성됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ResponseDto<ScheduleResponse>> createSchedule(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody ScheduleCreateRequest request) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        ScheduleResponse response = scheduleService.createSchedule(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.success(response, "스케줄이 생성되었습니다."));
    }

    @Operation(summary = "특정 날짜 스케줄 조회", description = "선택한 날짜의 모든 스케줄을 시간 순으로 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ResponseDto<List<ScheduleResponse>>> getSchedulesByDate(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "조회할 날짜", example = "2025-10-14")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDate(userId, date);
        return ResponseEntity.ok(ResponseDto.success(schedules, "스케줄 조회가 완료되었습니다."));
    }

    @Operation(summary = "스케줄 수정", description = "기존 스케줄의 정보를 수정합니다. 반복 일정인 경우 수정 범위를 선택할 수 있습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ResponseDto<ScheduleResponse>> updateSchedule(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "스케줄 ID") @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        ScheduleResponse response = scheduleService.updateSchedule(userId, scheduleId, request);
        return ResponseEntity.ok(ResponseDto.success(response, "스케줄이 수정되었습니다."));
    }

    @Operation(summary = "스케줄 삭제", description = "선택한 스케줄을 삭제합니다. 반복 일정인 경우 삭제 범위를 선택할 수 있습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ResponseDto<Void>> deleteSchedule(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "스케줄 ID") @PathVariable Long scheduleId,
            @Parameter(description = "삭제 범위", example = "THIS_ONLY")
            @RequestParam(required = false, defaultValue = "THIS_ONLY") DeleteScope deleteScope) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        scheduleService.deleteSchedule(userId, scheduleId, deleteScope);
        return ResponseEntity.ok(ResponseDto.success("스케줄이 삭제되었습니다."));
    }

    @Operation(summary = "월별 스케줄 날짜 목록", description = "특정 월에 스케줄이 있는 날짜 목록을 조회합니다. (캘린더 표시용)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/dates")
    public ResponseEntity<ResponseDto<List<LocalDate>>> getScheduleDatesInMonth(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Parameter(description = "년도", example = "2025") @RequestParam int year,
            @Parameter(description = "월 (1-12)", example = "10") @RequestParam int month) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        List<LocalDate> dates = scheduleService.getScheduleDatesInMonth(userId, year, month);
        return ResponseEntity.ok(ResponseDto.success(dates, "스케줄 날짜 조회가 완료되었습니다."));
    }

    /**
     * Bearer 토큰 추출 헬퍼 메서드
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "액세스 토큰이 필요합니다");
        }
        return authorizationHeader.substring(7);
    }
}