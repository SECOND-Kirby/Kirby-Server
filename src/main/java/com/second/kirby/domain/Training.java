package com.second.kirby.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Training extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 훈련 설정값
    @Column(name = "intensity")
    private Integer intensity;      // 강도 (1-10)

    @Column(name = "direction")
    private Integer direction;      // 방향 (1-10)

    @Column(name = "frequency")
    private Integer frequency;      // 빈도 (1-10)

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // 시간 (분)

    // 훈련 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TrainingStatus status = TrainingStatus.READY;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_pause_seconds")
    @Builder.Default
    private Long totalPauseSeconds = 0L;

    // 훈련 제어 메서드들
    public void start() {
        this.status = TrainingStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.pausedAt = null;
    }

    public void pause() {
        if (this.status == TrainingStatus.IN_PROGRESS) {
            this.status = TrainingStatus.PAUSED;
            this.pausedAt = LocalDateTime.now();
        }
    }

    public void resume() {
        if (this.status == TrainingStatus.PAUSED && this.pausedAt != null) {
            this.status = TrainingStatus.IN_PROGRESS;
            this.totalPauseSeconds += java.time.Duration.between(this.pausedAt, LocalDateTime.now()).getSeconds();
            this.pausedAt = null;
        }
    }

    public void complete() {
        this.status = TrainingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();

        if (this.pausedAt != null) {
            this.totalPauseSeconds += java.time.Duration.between(this.pausedAt, LocalDateTime.now()).getSeconds();
            this.pausedAt = null;
        }
    }

    public long getActualTrainingSeconds() {
        if (startedAt == null) return 0;

        LocalDateTime endTime = completedAt != null ? completedAt :
                (pausedAt != null ? pausedAt : LocalDateTime.now());

        long totalSeconds = java.time.Duration.between(startedAt, endTime).getSeconds();
        return Math.max(0, totalSeconds - totalPauseSeconds);
    }

    public enum TrainingStatus {
        READY,          // 준비
        IN_PROGRESS,    // 진행중
        PAUSED,         // 일시정지
        COMPLETED       // 완료
    }
}