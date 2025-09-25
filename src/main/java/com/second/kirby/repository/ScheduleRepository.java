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
    @Query("SELECT DISTINCT s.scheduleDate FROM Schedule s WHERE s.userId = :userId AND s.scheduleDate BETWEEN :startDate AND :endDate")
    List<LocalDate> findScheduleDatesInRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}