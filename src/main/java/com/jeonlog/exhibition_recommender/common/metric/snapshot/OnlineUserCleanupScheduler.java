package com.jeonlog.exhibition_recommender.common.metric.snapshot;

import com.jeonlog.exhibition_recommender.common.metric.MetricKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * online_users ZSET 에서 5분 이상 활동이 없는 멤버를 분당 1회 제거.
 * 청소가 주기적으로 돌기 때문에 onlineUserCount() 는 ZCARD 만으로 정확한 값을 반환할 수 있다.
 * 중복 실행되어도 같은 작업을 반복할 뿐 데이터가 손상되지 않으므로 ShedLock 은 적용하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OnlineUserCleanupScheduler {

    private static final long ONLINE_WINDOW_SECONDS = 300L;

    private final StringRedisTemplate redis;

    @Scheduled(fixedRate = 60_000L, initialDelay = 60_000L)
    public void evictStaleHeartbeats() {
        try {
            long cutoff = Instant.now().getEpochSecond() - ONLINE_WINDOW_SECONDS;
            Long removed = redis.opsForZSet()
                    .removeRangeByScore(MetricKeys.onlineUsers(), 0, cutoff);
            if (removed != null && removed > 0) {
                log.debug("[METRIC] online cleanup evicted={} cutoff={}", removed, cutoff);
            }
        } catch (Exception e) {
            log.warn("[METRIC] online cleanup failed reason={}", e.getMessage());
        }
    }
}
