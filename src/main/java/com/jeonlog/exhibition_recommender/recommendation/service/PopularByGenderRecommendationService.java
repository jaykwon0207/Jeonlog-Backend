package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.repository.PopularByGenderRecommendationRepository;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopularByGenderRecommendationService {

    private final PopularByGenderRecommendationRepository repo;
    private static final int MAX_DAYS = 365;
    private static final double MIN_WEIGHT = 0.0;
    private static final double MAX_WEIGHT = 10.0;

    @Transactional(readOnly = true)
    public List<RecommendationDto> getPopularByGender(int genderId, int days,
                                                      double clickWeight, double bookmarkWeight) {
        validateDays(days);
        validateWeights(clickWeight, bookmarkWeight);

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days);
        LocalDate toDate = today;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDt = now.minusDays(days);
        LocalDateTime toDt = now;

        Gender gender = mapGender(genderId);
        var top10 = PageRequest.of(0, 10);

        List<Long> ids = repo.findTopPopularByGender(
                today, fromDate, toDate, fromDt, toDt, gender, clickWeight, bookmarkWeight, top10);

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

    private static Gender mapGender(int id) {
        if (id == 0) return Gender.MALE;
        if (id == 1) return Gender.FEMALE;
        throw new IllegalArgumentException("id는 0(남) 또는 1(여)만 허용됩니다.");
    }
}
