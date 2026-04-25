package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DailyActiveUserStatRepository extends JpaRepository<DailyActiveUserStat, LocalDate> {
}
