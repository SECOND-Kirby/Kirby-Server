package com.second.kirby.service;

import com.second.kirby.domain.RecurringSchedule;
import com.second.kirby.domain.Schedule;
import com.second.kirby.dto.request.schedule.ScheduleCreateRequest;
import com.second.kirby.dto.request.schedule.ScheduleUpdateRequest;
import com.second.kirby.dto.response.ScheduleResponse;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.repository.RecurringScheduleRepository;
import com.second.kirby.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RecurringScheduleRepository recurringScheduleRepository;

    /**
     * 스케줄 생성
     * 반복 설정이 있는 경우 반복 일정을 생성합니다.
     */
    @Transactional
    public ScheduleResponse createSchedule(Long userId, ScheduleCreateRequest request) {
        // 반복 일정 검증
        if (request.repeatDays() != null && !request.repeatDays().isEmpty()) {
            if (request.repeatEndDate() == null) {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "반복 종료 날짜는 필수입니다.");
            }
            if (request.repeatEndDate().isBefore(request.scheduleDate())) {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "종료 날짜는 시작 날짜 이후여야 합니다.");
            }
            return createRecurringSchedule(userId, request);
        }

        // 일반 일정 생성
        Schedule schedule = Schedule.builder()
                .userId(userId)
                .title(request.title())
                .scheduleDate(request.scheduleDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .memo(request.memo())
                .isModified(false)
                .build();

        scheduleRepository.save(schedule);
        log.info("일반 일정 생성 완료 - userId: {}, scheduleId: {}", userId, schedule.getId());

        return ScheduleResponse.from(schedule, null);
    }

    /**
     * 반복 일정 생성
     * 반복 템플릿을 생성하고 지정된 요일에 맞춰 실제 일정들을 생성합니다.
     */
    private ScheduleResponse createRecurringSchedule(Long userId, ScheduleCreateRequest request) {
        // 1. RecurringSchedule 템플릿 생성
        RecurringSchedule recurring = RecurringSchedule.builder()
                .userId(userId)
                .title(request.title())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .memo(request.memo())
                .repeatDays(request.repeatDays())
                .repeatStartDate(request.scheduleDate())
                .repeatEndDate(request.repeatEndDate())
                .isActive(true)
                .build();

        recurringScheduleRepository.save(recurring);

        // 2. 실제 일정들 생성
        List<Schedule> schedules = new ArrayList<>();
        LocalDate currentDate = request.scheduleDate();

        while (!currentDate.isAfter(request.repeatEndDate())) {
            if (request.repeatDays().contains(currentDate.getDayOfWeek())) {
                Schedule schedule = Schedule.builder()
                        .userId(userId)
                        .title(request.title())
                        .scheduleDate(currentDate)
                        .startTime(request.startTime())
                        .endTime(request.endTime())
                        .memo(request.memo())
                        .recurringScheduleId(recurring.getId())
                        .isModified(false)
                        .build();

                schedules.add(schedule);
            }
            currentDate = currentDate.plusDays(1);
        }

        scheduleRepository.saveAll(schedules);
        log.info("반복 일정 생성 완료 - userId: {}, recurringId: {}, 생성된 일정 수: {}",
                userId, recurring.getId(), schedules.size());

        return ScheduleResponse.from(schedules.get(0), recurring);
    }

    /**
     * 특정 날짜의 스케줄 조회
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByDate(Long userId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository
                .findByUserIdAndScheduleDateOrderByStartTime(userId, date);

        return schedules.stream()
                .map(schedule -> {
                    RecurringSchedule recurring = null;
                    if (schedule.getRecurringScheduleId() != null) {
                        recurring = recurringScheduleRepository
                                .findById(schedule.getRecurringScheduleId())
                                .orElse(null);
                    }
                    return ScheduleResponse.from(schedule, recurring);
                })
                .collect(Collectors.toList());
    }

    /**
     * 월별 스케줄이 있는 날짜 목록 조회
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getScheduleDatesInMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return scheduleRepository.findScheduleDatesInRange(userId, startDate, endDate);
    }

    /**
     * 스케줄 수정
     * 수정 범위에 따라 개별/이후/전체 수정을 처리합니다.
     */
    @Transactional
    public ScheduleResponse updateSchedule(Long userId, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.SCHEDULE_NOT_FOUND));

        switch (request.updateScope()) {
            case THIS_ONLY -> updateSingleSchedule(schedule, request);
            case THIS_AND_FUTURE -> updateThisAndFutureSchedules(schedule, request, userId);
            case ALL -> updateAllRecurringSchedules(schedule, request);
        }

        RecurringSchedule recurring = null;
        if (schedule.getRecurringScheduleId() != null) {
            recurring = recurringScheduleRepository
                    .findById(schedule.getRecurringScheduleId())
                    .orElse(null);
        }

        return ScheduleResponse.from(schedule, recurring);
    }

    /**
     * 개별 일정만 수정
     * 반복 일정에서 분리하여 독립적으로 수정합니다.
     */
    private void updateSingleSchedule(Schedule schedule, ScheduleUpdateRequest request) {
        schedule.setRecurringScheduleId(null);
        schedule.setIsModified(true);
        schedule.setTitle(request.title());
        schedule.setScheduleDate(request.scheduleDate());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setMemo(request.memo());

        scheduleRepository.save(schedule);
        log.info("개별 일정 수정 완료 - scheduleId: {}", schedule.getId());
    }

    /**
     * 이후 모든 일정 수정
     * 기존 반복 일정을 현재 일정 전날까지로 변경하고, 새로운 반복 일정을 생성합니다.
     */
    private void updateThisAndFutureSchedules(Schedule schedule, ScheduleUpdateRequest request, Long userId) {
        Long recurringId = schedule.getRecurringScheduleId();
        if (recurringId == null) {
            updateSingleSchedule(schedule, request);
            return;
        }

        RecurringSchedule recurring = recurringScheduleRepository.findById(recurringId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RECURRING_SCHEDULE_NOT_FOUND));

        // 기존 반복 일정의 종료일 변경
        LocalDate originalEndDate = recurring.getRepeatEndDate();
        recurring.setRepeatEndDate(schedule.getScheduleDate().minusDays(1));
        recurringScheduleRepository.save(recurring);

        // 현재 날짜 이후 일정 삭제
        scheduleRepository.deleteByRecurringScheduleIdAndScheduleDateGreaterThanEqual(
                recurringId,
                schedule.getScheduleDate()
        );

        // 새로운 반복 일정 생성
        ScheduleCreateRequest newRequest = new ScheduleCreateRequest(
                request.title(),
                request.scheduleDate(),
                request.startTime(),
                request.endTime(),
                request.memo(),
                recurring.getRepeatDays(),
                originalEndDate
        );

        createRecurringSchedule(userId, newRequest);
        log.info("이후 일정 수정 완료 - 기존 recurringId: {}", recurringId);
    }

    /**
     * 전체 반복 일정 수정
     * 반복 템플릿과 개별 수정되지 않은 모든 일정을 수정합니다.
     */
    private void updateAllRecurringSchedules(Schedule schedule, ScheduleUpdateRequest request) {
        Long recurringId = schedule.getRecurringScheduleId();
        if (recurringId == null) {
            updateSingleSchedule(schedule, request);
            return;
        }

        RecurringSchedule recurring = recurringScheduleRepository.findById(recurringId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RECURRING_SCHEDULE_NOT_FOUND));

        // 반복 템플릿 수정
        recurring.setTitle(request.title());
        recurring.setStartTime(request.startTime());
        recurring.setEndTime(request.endTime());
        recurring.setMemo(request.memo());
        recurringScheduleRepository.save(recurring);

        // 개별 수정되지 않은 모든 일정 수정
        List<Schedule> relatedSchedules = scheduleRepository
                .findByRecurringScheduleIdAndIsModifiedFalse(recurringId);

        relatedSchedules.forEach(s -> {
            s.setTitle(request.title());
            s.setStartTime(request.startTime());
            s.setEndTime(request.endTime());
            s.setMemo(request.memo());
        });

        scheduleRepository.saveAll(relatedSchedules);
        log.info("전체 반복 일정 수정 완료 - recurringId: {}, 수정된 일정 수: {}",
                recurringId, relatedSchedules.size());
    }

    /**
     * 스케줄 삭제
     * 삭제 범위에 따라 개별/이후/전체 삭제를 처리합니다.
     */
    @Transactional
    public void deleteSchedule(Long userId, Long scheduleId, DeleteScope scope) {
        Schedule schedule = scheduleRepository.findByIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.SCHEDULE_NOT_FOUND));

        switch (scope) {
            case THIS_ONLY -> {
                scheduleRepository.delete(schedule);
                log.info("개별 일정 삭제 - scheduleId: {}", scheduleId);
            }
            case THIS_AND_FUTURE -> deleteThisAndFutureSchedules(schedule);
            case ALL -> deleteAllRecurringSchedules(schedule);
        }
    }

    /**
     * 이후 모든 일정 삭제
     */
    private void deleteThisAndFutureSchedules(Schedule schedule) {
        Long recurringId = schedule.getRecurringScheduleId();
        if (recurringId == null) {
            scheduleRepository.delete(schedule);
            return;
        }

        RecurringSchedule recurring = recurringScheduleRepository.findById(recurringId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RECURRING_SCHEDULE_NOT_FOUND));

        recurring.setRepeatEndDate(schedule.getScheduleDate().minusDays(1));
        recurringScheduleRepository.save(recurring);

        scheduleRepository.deleteByRecurringScheduleIdAndScheduleDateGreaterThanEqual(
                recurringId,
                schedule.getScheduleDate()
        );

        log.info("이후 일정 삭제 완료 - recurringId: {}", recurringId);
    }

    /**
     * 전체 반복 일정 삭제
     */
    private void deleteAllRecurringSchedules(Schedule schedule) {
        Long recurringId = schedule.getRecurringScheduleId();
        if (recurringId == null) {
            scheduleRepository.delete(schedule);
            return;
        }

        RecurringSchedule recurring = recurringScheduleRepository.findById(recurringId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RECURRING_SCHEDULE_NOT_FOUND));

        scheduleRepository.deleteByRecurringScheduleId(recurringId);

        recurring.setIsActive(false);
        recurringScheduleRepository.save(recurring);

        log.info("전체 반복 일정 삭제 완료 - recurringId: {}", recurringId);
    }

    /**
     * 삭제 범위
     */
    public enum DeleteScope {
        THIS_ONLY,          // 이 일정만
        THIS_AND_FUTURE,    // 이후 모든 일정
        ALL                 // 전체 반복 일정
    }
}