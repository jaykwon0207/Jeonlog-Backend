package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import com.jeonlog.exhibition_recommender.common.metric.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ExhibitionRankingSnapshotRepository
        extends JpaRepository<ExhibitionRankingSnapshot, Long> {

    @Modifying
    @Query("delete from ExhibitionRankingSnapshot s " +
            "where s.snapshotAt = :snapshotAt " +
            "  and s.action = :action " +
            "  and s.windowType = :windowType")
    void deleteBySnapshotAtAndActionAndWindowType(
            @Param("snapshotAt") LocalDateTime snapshotAt,
            @Param("action") Action action,
            @Param("windowType") WindowType windowType);
}
