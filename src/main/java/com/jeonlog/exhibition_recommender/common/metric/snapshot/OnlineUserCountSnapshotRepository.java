package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface OnlineUserCountSnapshotRepository
        extends JpaRepository<OnlineUserCountSnapshot, LocalDateTime> {
}
