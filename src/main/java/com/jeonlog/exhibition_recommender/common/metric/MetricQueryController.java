package com.jeonlog.exhibition_recommender.common.metric;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricQueryController {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MetricQueryService queryService;

    @GetMapping("/dau")
    public ApiResponse<Long> dau(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now(KST);
        return ApiResponse.ok(queryService.dau(target));
    }

    @GetMapping("/mau")
    public ApiResponse<Long> mau(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(queryService.mau(from, to));
    }

    @GetMapping("/retention")
    public ApiResponse<MetricQueryService.RetentionResult> retention(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cohort,
            @RequestParam("return") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
        return ApiResponse.ok(queryService.retention(cohort, returnDate));
    }

    @GetMapping("/hour-distribution")
    public ApiResponse<Map<Integer, Long>> hourDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now(KST);
        return ApiResponse.ok(queryService.hourDistribution(target));
    }

    @GetMapping("/rank")
    public ApiResponse<List<MetricQueryService.RankEntry>> rank(
            @RequestParam Action action,
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int limit) {
        LocalDate target = date != null ? date : LocalDate.now(KST);
        return ApiResponse.ok(queryService.topRank(action, type, target, limit));
    }

    @GetMapping("/counter")
    public ApiResponse<Long> counter(
            @RequestParam Action action,
            @RequestParam String type,
            @RequestParam String id) {
        return ApiResponse.ok(queryService.counter(action, type, id));
    }

    @GetMapping("/online")
    public ApiResponse<Long> online() {
        return ApiResponse.ok(queryService.onlineUserCount());
    }
}
