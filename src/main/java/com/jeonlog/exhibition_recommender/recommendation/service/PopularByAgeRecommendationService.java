package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.repository.PopularByAgeRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopularByAgeRecommendationService {

    private final PopularByAgeRecommendationRepository repo;
    private static final int MAX_DAYS = 365;
    private static final double MIN_WEIGHT = 0.0;
    private static final double MAX_WEIGHT = 10.0;

    @Transactional(readOnly = true)
    public List<RecommendationDto> getPopularByAgeGroup(
            int ageGroup, int days,
            double clickWeight, double bookmarkWeight
    ) {
        validateDays(days);
        validateWeights(clickWeight, bookmarkWeight);

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days);
        LocalDate toDate = today;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDt = now.minusDays(days);
        LocalDateTime toDt = now;

        int currentYear = Year.now().getValue();
        int[] yr = toBirthYearRangeFromGroup(ageGroup, currentYear);
        int minBirthYear = yr[0];
        int maxBirthYear = yr[1];

        // 상위 10개 전시 조회
        var top10 = PageRequest.of(0, 10);
        List<Long> ids = repo.findTopPopularByAge(
                today,
                fromDate, toDate, fromDt, toDt,
                minBirthYear, maxBirthYear,
                clickWeight, bookmarkWeight,
                top10
        );
        if (ids.isEmpty()) return List.of();

        Map<Long, Exhibition> byId = repo.findAllById(ids).stream()
                .collect(Collectors.toMap(Exhibition::getId, e -> e));

        List<Exhibition> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Exhibition e = byId.get(id);
            if (e != null) ordered.add(e);
        }
        return ordered.stream().map(RecommendationDto::from).toList();
    }

    private static void validateDays(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("days는 1 이상이어야 합니다.");
        }
        if (days > MAX_DAYS) {
            throw new IllegalArgumentException("days는 365 이하여야 합니다.");
        }
    }

    private static void validateWeights(double clickWeight, double bookmarkWeight) {
        if (!Double.isFinite(clickWeight) || !Double.isFinite(bookmarkWeight)) {
            throw new IllegalArgumentException("가중치는 유한한 숫자여야 합니다.");
        }
        if (clickWeight < MIN_WEIGHT || clickWeight > MAX_WEIGHT
                || bookmarkWeight < MIN_WEIGHT || bookmarkWeight > MAX_WEIGHT) {
            throw new IllegalArgumentException("가중치는 0.0 이상 10.0 이하여야 합니다.");
        }
    }

    private static int[] toBirthYearRangeFromGroup(int ageGroup, int currentYear) {
        switch (ageGroup) {
            case 1: //유아청소년 (~19세)
                return new int[]{ currentYear - 19, currentYear };
            case 2: //20~39세 (2~30대)
                return new int[]{ currentYear - 39, currentYear - 20 };
            case 3: //40~59세 (4~50대)
                return new int[]{ currentYear - 59, currentYear - 40 };
            case 4: //60세 이상
                return new int[]{ 0, currentYear - 60 };
            default:
                throw new IllegalArgumentException("ageGroup은 1(유아청소년), 2(20-30대), 3(40-50대), 4(60대 이상) 중 하나여야 합니다.");
        }
    }
}
