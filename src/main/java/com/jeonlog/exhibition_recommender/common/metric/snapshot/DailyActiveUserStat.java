package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_active_user_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyActiveUserStat {

    @Id
    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "dau", nullable = false)
    private long dau;

    @Column(name = "mau_30d", nullable = false)
    private long mau30d;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void update(long dau, long mau30d, LocalDateTime updatedAt) {
        this.dau = dau;
        this.mau30d = mau30d;
        this.updatedAt = updatedAt;
    }
}
