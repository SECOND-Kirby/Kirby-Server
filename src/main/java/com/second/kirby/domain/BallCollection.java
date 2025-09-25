package com.second.kirby.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ball_collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallCollection extends BaseEntity {

    public enum Status {
        READY,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_count")
    private Integer targetCount;

    @Column(name = "collected_count")
    @Builder.Default
    private Integer collectedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.READY;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public void start() {
        this.status = Status.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.collectedCount = 0;
    }

    public void updateCount(int count) {
        this.collectedCount = count;
    }

    public void complete() {
        this.status = Status.COMPLETED;
        this.endedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.endedAt = LocalDateTime.now();
    }
}
