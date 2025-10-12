package com.second.kirby.repository;

import com.second.kirby.domain.BallCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BallCollectionRepository extends JpaRepository<BallCollection, Long> {
    Optional<BallCollection> findByUserIdAndStatus(Long userId, BallCollection.Status status);

    // 특정 사용자의 모든 공 수거 기록 삭제
    void deleteByUserId(Long userId);
}
