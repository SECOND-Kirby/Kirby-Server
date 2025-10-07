package com.second.kirby.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "recurring_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RecurringSchedule extends BaseEntity{

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(length = 500)
    private String memo;

    // 반복 요일 목록 (월, 수, 금 등)
    @ElementCollection
    @CollectionTable(
            name = "recurring_schedule_days",
            joinColumns = @JoinColumn(name = "recurring_schedule_id")
    )
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> repeatDays;

    // 반복 시작 날짜
    @Column(nullable = false)
    private LocalDate repeatStartDate;

    // 반복 종료 날짜
    @Column(nullable = false)
    private LocalDate repeatEndDate;

    // 활성화 상태 (소프트 삭제용)
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
