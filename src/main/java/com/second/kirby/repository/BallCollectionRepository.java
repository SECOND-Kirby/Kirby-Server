package com.second.kirby.repository;

import com.second.kirby.domain.BallCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BallCollectionRepository extends JpaRepository<BallCollection, Long> {
    Optional<BallCollection> findByUserIdAndStatus(Long userId, BallCollection.Status status);
}
