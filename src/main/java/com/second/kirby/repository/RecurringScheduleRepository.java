package com.second.kirby.repository;

import com.second.kirby.domain.RecurringSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringScheduleRepository extends JpaRepository<RecurringSchedule, Long> {

    // 활성화된 반복 일정 조회
    List<RecurringSchedule> findByUserIdAndIsActiveTrue(Long userId);

    // ID와 사용자로 조회 (권한 확인용)
    Optional<RecurringSchedule> findByIdAndUserId(Long id, Long userId);

    // 존재 여부 확인
    boolean existsByIdAndUserId(Long id, Long userId);

    // 특정 사용자의 모든 반복 일정 삭제
    void deleteByUserId(Long userId);
}