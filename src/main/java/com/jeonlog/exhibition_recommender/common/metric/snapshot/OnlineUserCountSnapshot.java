package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "online_user_count_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OnlineUserCountSnapshot {

    @Id
    @Column(name = "snapshot_at")
    private LocalDateTime snapshotAt;

    @Column(name = "online_count", nullable = false)
    private long onlineCount;
}
