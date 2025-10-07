package com.second.kirby.repository;

import com.second.kirby.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 날짜의 스케줄 조회 (시작 시간 순 정렬)
    List<Schedule> findByUserIdAndScheduleDateOrderByStartTime(Long userId, LocalDate scheduleDate);

    // 사용자의 특정 스케줄 조회
    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    // 사용자의 월별 스케줄이 있는 날짜 목록
    @Query("SELECT DISTINCT s.scheduleDate FROM Schedule s " +
            "WHERE s.userId = :userId " +
            "AND s.scheduleDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.scheduleDate")
    List<LocalDate> findScheduleDatesInRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 반복 일정 ID로 조회 (개별 수정되지 않은 것만)
    List<Schedule> findByRecurringScheduleIdAndIsModifiedFalse(Long recurringScheduleId);

    // 반복 일정 ID로 모든 일정 조회
    List<Schedule> findByRecurringScheduleId(Long recurringScheduleId);

    // 특정 날짜 이후의 반복 일정 삭제
    void deleteByRecurringScheduleIdAndScheduleDateGreaterThanEqual(
            Long recurringScheduleId,
            LocalDate date
    );

    // 반복 일정 전체 삭제
    void deleteByRecurringScheduleId(Long recurringScheduleId);

    // 사용자의 특정 일정 존재 여부 확인
    boolean existsByIdAndUserId(Long id, Long userId);
}