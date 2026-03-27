package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.repository.PopularRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopularRecommendationService {
    private final PopularRecommendationRepository popularRepository;
    private static final int MAX_DAYS = 365;
    private static final double MIN_WEIGHT = 0.0;
    private static final double MAX_WEIGHT = 10.0;

    @Transactional(readOnly = true)
    public List<RecommendationDto> getPopular(int days, double clickWeight, double bookmarkWeight) {
        validateDays(days);
        validateWeights(clickWeight, bookmarkWeight);

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days);
        LocalDate toDate = today;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDt = now.minusDays(days);
        LocalDateTime toDt = now;

        var top10 = org.springframework.data.domain.PageRequest.of(0, 10);

        List<Long> ids = popularRepository.findTopPopularExhibitionIds(
                today, fromDate, toDate, fromDt, toDt, clickWeight, bookmarkWeight, top10);

        if (ids.isEmpty()) return List.of();

        Map<Long, Exhibition> byId = popularRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Exhibition::getId, e -> e));

        List<Exhibition> ordered = new java.util.ArrayList<>(ids.size());
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

}
