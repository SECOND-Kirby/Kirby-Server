package com.second.kirby.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "robot_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RobotSession extends BaseEntity {

    @Column(name = "connected_user_id")
    private Long connectedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RobotState state = RobotState.IDLE;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "session_timeout_minutes")
    @Builder.Default
    private Integer sessionTimeoutMinutes = 30;

    // 연결 상태 확인
    public boolean isConnected() {
        return connectedUserId != null && state != RobotState.IDLE;
    }

    // 세션 만료 확인
    public boolean isExpired() {
        if (lastHeartbeat == null) return false;
        return lastHeartbeat.isBefore(LocalDateTime.now().minusMinutes(sessionTimeoutMinutes));
    }

    // 특정 사용자가 연결된 상태인지 확인
    public boolean isConnectedBy(Long userId) {
        return connectedUserId != null && connectedUserId.equals(userId);
    }

    // 연결 설정
    public void connect(Long userId) {
        this.connectedUserId = userId;
        this.state = RobotState.CONNECTED;
        this.connectedAt = LocalDateTime.now();
        this.lastHeartbeat = LocalDateTime.now();
    }

    // 연결 해제
    public void disconnect() {
        this.connectedUserId = null;
        this.state = RobotState.IDLE;
        this.connectedAt = null;
        this.lastHeartbeat = null;
    }

    // 하트비트 업데이트
    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }

    public enum RobotState {
        IDLE,           // 대기중
        CONNECTED,      // 연결됨
        TRAINING        // 훈련중
    }
}