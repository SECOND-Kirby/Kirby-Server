package com.second.kirby.service;

import com.second.kirby.domain.Schedule;
import com.second.kirby.dto.request.schedule.ScheduleCreateRequest;
import com.second.kirby.dto.request.schedule.ScheduleUpdateRequest;
import com.second.kirby.dto.response.ScheduleResponse;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    // ===== 스케줄 생성 =====
    @Transactional
    public ScheduleResponse createSchedule(Long userId, ScheduleCreateRequest request) {
        log.info("스케줄 생성 요청: userId={}, title={}, date={}", userId, request.title(), request.scheduleDate());

        // 시간 유효성 검증
        validateScheduleTime(request.startTime(), request.endTime());

        // 스케줄 생성 (User 엔티티 조회 없이 userId만 사용)
        Schedule schedule = Schedule.builder()
                .title(request.title())
                .scheduleDate(request.scheduleDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .memo(request.memo())
                .userId(userId)  // userId 직접 설정
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("스케줄 생성 완료: scheduleId={}", savedSchedule.getId());

        return ScheduleResponse.from(savedSchedule);
    }

    // ===== 특정 날짜 스케줄 조회 =====
    public List<ScheduleResponse> getSchedulesByDate(Long userId, LocalDate date) {
        log.info("날짜별 스케줄 조회: userId={}, date={}", userId, date);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndScheduleDateOrderByStartTime(userId, date);

        return schedules.stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    // ===== 스케줄 수정 =====
    @Transactional
    public ScheduleResponse updateSchedule(Long userId, Long scheduleId, ScheduleUpdateRequest request) {
        log.info("스케줄 수정 요청: userId={}, scheduleId={}", userId, scheduleId);

        // 시간 유효성 검증
        validateScheduleTime(request.startTime(), request.endTime());

        // 스케줄 조회 (본인 스케줄인지 확인)
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.SCHEDULE_NOT_FOUND));

        // 스케줄 정보 업데이트
        schedule.setTitle(request.title());
        schedule.setScheduleDate(request.scheduleDate());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setMemo(request.memo());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("스케줄 수정 완료: scheduleId={}", updatedSchedule.getId());

        return ScheduleResponse.from(updatedSchedule);
    }

    // ===== 스케줄 삭제 =====
    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId) {
        log.info("스케줄 삭제 요청: userId={}, scheduleId={}", userId, scheduleId);

        // 스케줄 조회 (본인 스케줄인지 확인)
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.SCHEDULE_NOT_FOUND));

        scheduleRepository.delete(schedule);
        log.info("스케줄 삭제 완료: scheduleId={}", scheduleId);
    }

    // ===== 월별 스케줄이 있는 날짜 목록 조회 =====
    public List<LocalDate> getScheduleDatesInMonth(Long userId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "월은 1-12 범위여야 합니다");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return scheduleRepository.findScheduleDatesInRange(userId, startDate, endDate);
    }

    // ===== 시간 유효성 검증 =====
    private void validateScheduleTime(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new BusinessException(ResponseCode.INVALID_SCHEDULE_TIME);
        }
    }
}