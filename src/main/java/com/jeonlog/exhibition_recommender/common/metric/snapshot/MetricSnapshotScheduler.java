package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import com.jeonlog.exhibition_recommender.common.metric.Action;
import com.jeonlog.exhibition_recommender.common.metric.MetricQueryService;
import com.jeonlog.exhibition_recommender.common.metric.MetricQueryService.RankEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricSnapshotScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final List<Action> RANKING_ACTIONS = List.of(Action.VIEW, Action.CREATE);
    private static final String RANKING_TYPE = "exhibition";
    private static final int RANKING_TOP_N = 50;
    private static final int MAU_WINDOW_DAYS = 30;

    private final MetricQueryService queryService;
    private final DailyActiveUserStatRepository dauRepository;
    private final ExhibitionRankingSnapshotRepository rankingRepository;
    private final OnlineUserCountSnapshotRepository onlineUserRepository;

    /**
     * 매일 00:05 KST - 어제자 DAU + 어제 기준 30일 MAU 를 RDS 에 저장.
     * 자정 직후 실행되므로 어제 23~24시 활동까지 모두 반영된다.
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "metricSnapshot_dailyActiveUser",
            lockAtMostFor = "PT5M", lockAtLeastFor = "PT30S")
    @Transactional
    public void snapshotDailyActiveUserStat() {
        LocalDate yesterday = LocalDate.now(KST).minusDays(1);
        LocalDateTime now = LocalDateTime.now(KST);

        try {
            long dau = queryService.dau(yesterday);
            long mau = queryService.mau(yesterday.minusDays(MAU_WINDOW_DAYS - 1L), yesterday);

            DailyActiveUserStat stat = dauRepository.findById(yesterday)
                    .orElseGet(() -> DailyActiveUserStat.builder()
                            .statDate(yesterday)
                            .dau(0L)
                            .mau30d(0L)
                            .updatedAt(now)
                            .build());
            stat.update(dau, mau, now);
            dauRepository.save(stat);

            log.info("[METRIC-SNAPSHOT] active-user date={} dau={} mau30d={}",
                    yesterday, dau, mau);
        } catch (Exception e) {
            log.error("[METRIC-SNAPSHOT] active-user failed date={} reason={}",
                    yesterday, e.getMessage(), e);
        }
    }

    /**
     * 매 5분 - 오늘 기준 일별/주간 전시 랭킹 Top N 을 RDS 에 저장.
     * - DAILY : 오늘자 ZSET 직접
     * - WEEKLY: 오늘 포함 최근 7일치 ZSET 을 ZUNIONSTORE 로 합산
     */
    @Scheduled(cron = "0 0/5 * * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "metricSnapshot_ranking",
            lockAtMostFor = "PT5M", lockAtLeastFor = "PT15S")
    @Transactional
    public void snapshotRanking() {
        LocalDateTime snapshotAt = LocalDateTime.now(KST).withSecond(0).withNano(0);
        LocalDate today = snapshotAt.toLocalDate();

        try {
            for (Action action : RANKING_ACTIONS) {
                List<RankEntry> daily = queryService.topRank(
                        action, RANKING_TYPE, today, RANKING_TOP_N);
                persistRanking(snapshotAt, action, WindowType.DAILY, daily);

                List<RankEntry> weekly = queryService.weeklyTopRank(
                        action, RANKING_TYPE, today, RANKING_TOP_N);
                persistRanking(snapshotAt, action, WindowType.WEEKLY, weekly);
            }
        } catch (Exception e) {
            log.error("[METRIC-SNAPSHOT] ranking failed at={} reason={}",
                    snapshotAt, e.getMessage(), e);
        }
    }

    private void persistRanking(LocalDateTime snapshotAt,
                                Action action,
                                WindowType windowType,
                                List<RankEntry> entries) {
        rankingRepository.deleteBySnapshotAtAndActionAndWindowType(
                snapshotAt, action, windowType);
        if (entries.isEmpty()) return;

        int rank = 1;
        for (RankEntry entry : entries) {
            Long exhibitionId = parseId(entry.id());
            if (exhibitionId == null) continue;

            rankingRepository.save(ExhibitionRankingSnapshot.builder()
                    .snapshotAt(snapshotAt)
                    .action(action)
                    .windowType(windowType)
                    .exhibitionId(exhibitionId)
                    .score(entry.score())
                    .rankPosition(rank++)
                    .build());
        }
        log.info("[METRIC-SNAPSHOT] ranking action={} window={} saved={}",
                action, windowType, entries.size());
    }

    private Long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            log.warn("[METRIC-SNAPSHOT] skip non-numeric ranking id={}", raw);
            return null;
        }
    }

    /**
     * 매 분 정각 - 현재 동시접속자수를 RDS 에 기록 (시계열 차트용).
     * snapshot_at 을 분 단위로 정규화해 PK 충돌 시 UPSERT 동작.
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "metricSnapshot_onlineUser",
            lockAtMostFor = "PT2M", lockAtLeastFor = "PT5S")
    @Transactional
    public void snapshotOnlineUserCount() {
        LocalDateTime snapshotAt = LocalDateTime.now(KST).withSecond(0).withNano(0);
        try {
            long count = queryService.onlineUserCount();
            onlineUserRepository.save(OnlineUserCountSnapshot.builder()
                    .snapshotAt(snapshotAt)
                    .onlineCount(count)
                    .build());
            log.debug("[METRIC-SNAPSHOT] online-user at={} count={}", snapshotAt, count);
        } catch (Exception e) {
            log.error("[METRIC-SNAPSHOT] online-user failed at={} reason={}",
                    snapshotAt, e.getMessage(), e);
        }
    }
}
