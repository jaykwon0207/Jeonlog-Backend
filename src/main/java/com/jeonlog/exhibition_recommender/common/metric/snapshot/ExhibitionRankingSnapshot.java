package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import com.jeonlog.exhibition_recommender.common.metric.Action;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exhibition_ranking_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_snapshot_action_window_exhibition",
                        columnNames = {"snapshot_at", "action", "window_type", "exhibition_id"}
                )
        },
        indexes = {
                @Index(name = "idx_snapshot_at_action_window",
                        columnList = "snapshot_at, action, window_type")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExhibitionRankingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_at", nullable = false)
    private LocalDateTime snapshotAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;

    @Enumerated(EnumType.STRING)
    @Column(name = "window_type", nullable = false, length = 10)
    private WindowType windowType;

    @Column(name = "exhibition_id", nullable = false)
    private Long exhibitionId;

    @Column(name = "score", nullable = false)
    private long score;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;
}
