package com.jeonlog.exhibition_recommender.common.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MetricQueryService {

    private final StringRedisTemplate redis;

    public long dau(LocalDate date) {
        return bitCount(MetricKeys.dau(date));
    }

    public long mau(LocalDate from, LocalDate to) {
        List<byte[]> keys = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            keys.add(MetricKeys.dau(d).getBytes(StandardCharsets.UTF_8));
        }
        String tmpKey = "mau:tmp:" + UUID.randomUUID();
        byte[] tmpBytes = tmpKey.getBytes(StandardCharsets.UTF_8);
        try {
            redis.execute((RedisCallback<Long>) conn -> conn.stringCommands()
                    .bitOp(RedisStringCommands.BitOperation.OR, tmpBytes, keys.toArray(new byte[0][])));
            return bitCount(tmpKey);
        } finally {
            redis.delete(tmpKey);
        }
    }

    public RetentionResult retention(LocalDate cohortDate, LocalDate returnDate) {
        String cohortKey = MetricKeys.dau(cohortDate);
        String returnKey = MetricKeys.dau(returnDate);
        String tmpKey = "retention:tmp:" + UUID.randomUUID();
        byte[] tmpBytes = tmpKey.getBytes(StandardCharsets.UTF_8);
        try {
            redis.execute((RedisCallback<Long>) conn -> conn.stringCommands()
                    .bitOp(RedisStringCommands.BitOperation.AND, tmpBytes,
                            cohortKey.getBytes(StandardCharsets.UTF_8),
                            returnKey.getBytes(StandardCharsets.UTF_8)));
            long cohortSize = bitCount(cohortKey);
            long retained = bitCount(tmpKey);
            double rate = cohortSize == 0 ? 0.0 : (double) retained / cohortSize;
            return new RetentionResult(cohortDate, returnDate, cohortSize, retained, rate);
        } finally {
            redis.delete(tmpKey);
        }
    }

    public long counter(Action action, String type, Object id) {
        String v = redis.opsForValue().get(MetricKeys.counter(action, type, id));
        return v == null ? 0L : Long.parseLong(v);
    }

    public List<RankEntry> topRank(Action action, String type, LocalDate date, int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redis.opsForZSet()
                .reverseRangeWithScores(MetricKeys.rank(action, type, date), 0, limit - 1);
        if (tuples == null) return List.of();
        List<RankEntry> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            list.add(new RankEntry(t.getValue(), t.getScore() == null ? 0L : t.getScore().longValue()));
        }
        return list;
    }

    public Map<Integer, Long> hourDistribution(LocalDate date) {
        Map<Object, Object> raw = redis.opsForHash().entries(MetricKeys.hourDist(date));
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) result.put(h, 0L);
        for (Map.Entry<Object, Object> e : raw.entrySet()) {
            int hour = Integer.parseInt(e.getKey().toString());
            long count = Long.parseLong(e.getValue().toString());
            result.put(hour, count);
        }
        return result;
    }

    private long bitCount(String key) {
        Long v = redis.execute((RedisCallback<Long>) conn -> conn.stringCommands()
                .bitCount(key.getBytes(StandardCharsets.UTF_8)));
        return v == null ? 0L : v;
    }

    public record RankEntry(String id, long score) {}

    public record RetentionResult(LocalDate cohortDate, LocalDate returnDate,
                                  long cohortSize, long retained, double rate) {}
}
