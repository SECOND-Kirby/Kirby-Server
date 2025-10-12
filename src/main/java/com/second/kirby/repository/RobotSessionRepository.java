package com.second.kirby.repository;

import com.second.kirby.domain.RobotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RobotSessionRepository extends JpaRepository<RobotSession, Long> {

    // 현재 활성 세션 조회 (로봇은 1대이므로 활성 세션은 최대 1개)
    @Query("SELECT rs FROM RobotSession rs WHERE rs.connectedUserId IS NOT NULL ORDER BY rs.connectedAt DESC")
    Optional<RobotSession> findActiveSession();

    // 특정 사용자의 활성 세션 조회
    Optional<RobotSession> findByConnectedUserId(Long userId);

    // 만료된 세션들 조회
    @Query("SELECT rs FROM RobotSession rs WHERE rs.lastHeartbeat < :expireTime AND rs.connectedUserId IS NOT NULL")
    List<RobotSession> findExpiredSessions(LocalDateTime expireTime);


}
