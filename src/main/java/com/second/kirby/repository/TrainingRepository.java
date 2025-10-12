package com.second.kirby.repository;

import com.second.kirby.domain.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    // 특정 사용자의 진행중인 훈련 조회
    Optional<Training> findByUserIdAndStatusIn(Long userId, List<Training.TrainingStatus> statuses);

    // 특정 사용자의 모든 훈련 기록 삭제
    void deleteByUserId(Long userId);

}
