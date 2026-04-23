package com.jeonlog.exhibition_recommender.common.metric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricRecorder {

    private final StringRedisTemplate redis;

    @Async
    public void recordEvent(Action action, String type, Object id, boolean rank) {
        try {
            LocalDate today = LocalDate.now();
            redis.opsForValue().increment(MetricKeys.counter(action, type, id));
            if (rank) {
                redis.opsForZSet().incrementScore(
                        MetricKeys.rank(action, type, today), id.toString(), 1);
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
            LocalDate today = LocalDate.now();
            int hour = LocalDateTime.now().getHour();
            redis.opsForValue().setBit(MetricKeys.dau(today), userId, true);
            redis.opsForHash().increment(MetricKeys.hourDist(today), String.format("%02d", hour), 1);
        } catch (Exception e) {
            log.warn("[METRIC] recordActiveUser failed userId={} reason={}", userId, e.getMessage());
        }
    }
}
