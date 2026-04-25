package com.jeonlog.exhibition_recommender.common.metric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricRecorder {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final Duration ACTIVE_USER_TTL = Duration.ofDays(35);
    private static final Duration RANK_TTL = Duration.ofDays(14);

    private final StringRedisTemplate redis;

    @Async
    public void recordEvent(Action action, String type, Object id, boolean rank) {
        try {
            LocalDate today = LocalDate.now(KST);
            // count:* 는 전시별 총 누적 카운터라 만료시키지 않는다
            redis.opsForValue().increment(MetricKeys.counter(action, type, id));
            if (rank) {
                String rankKey = MetricKeys.rank(action, type, today);
                redis.opsForZSet().incrementScore(rankKey, id.toString(), 1);
                // 7일 WEEKLY 집계용 + 버퍼
                redis.expire(rankKey, RANK_TTL);
            }
        } catch (Exception e) {
            log.warn("[METRIC] recordEvent failed action={} type={} id={} reason={}",
                    action, type, id, e.getMessage());
        }
    }

    @Async
    public void recordActiveUser(Long userId) {
        if (userId == null) return;
        try {
            LocalDate today = LocalDate.now(KST);
            int hour = LocalDateTime.now(KST).getHour();
            String dauKey = MetricKeys.dau(today);
            String hourKey = MetricKeys.hourDist(today);
            redis.opsForValue().setBit(dauKey, userId, true);
            redis.opsForHash().increment(hourKey, String.format("%02d", hour), 1);
            // 30일 MAU 계산용 + 5일 버퍼
            redis.expire(dauKey, ACTIVE_USER_TTL);
            redis.expire(hourKey, ACTIVE_USER_TTL);
        } catch (Exception e) {
            log.warn("[METRIC] recordActiveUser failed userId={} reason={}", userId, e.getMessage());
        }
    }

    /**
     * 실시간 접속자(하트비트) 갱신.
     * 같은 userId 는 ZSET 멤버 중복 없이 score(epoch)만 갱신되므로
     * 자연스럽게 "마지막 활동 시각" 기준으로 관리된다.
     */
    @Async
    public void recordHeartbeat(Long userId) {
        if (userId == null) return;
        try {
            long now = Instant.now().getEpochSecond();
            redis.opsForZSet().add(MetricKeys.onlineUsers(), userId.toString(), now);
        } catch (Exception e) {
            log.warn("[METRIC] recordHeartbeat failed userId={} reason={}", userId, e.getMessage());
        }
    }
}
