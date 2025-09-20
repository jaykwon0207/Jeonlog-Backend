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

    @Transactional(readOnly = true)
    public List<RecommendationDto> getPopularByAgeDecade(int decade, int days,
                                                         double clickWeight, double bookmarkWeight) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days);
        LocalDate toDate = today;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDt = now.minusDays(days);
        LocalDateTime toDt = now;

        //출생연도 범위 (decade=10,20,30,40,50,60(이상))
        int currentYear = Year.now().getValue();
        int[] yr = toBirthYearRangeFromDecade(decade, currentYear); // [min, max]
        int minBirthYear = yr[0];
        int maxBirthYear = yr[1];

        //Top10 조회
        var top10 = PageRequest.of(0, 10);
        List<Long> ids = repo.findTopPopularByAge(
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

    private static int[] toBirthYearRangeFromDecade(int decade, int currentYear) {
        switch (decade) {
            case 10: return new int[]{ currentYear - 19, currentYear - 10 };
            case 20: return new int[]{ currentYear - 29, currentYear - 20 };
            case 30: return new int[]{ currentYear - 39, currentYear - 30 };
            case 40: return new int[]{ currentYear - 49, currentYear - 40 };
            case 50: return new int[]{ currentYear - 59, currentYear - 50 };
            case 60: // 60대 이상
                return new int[]{ 0, currentYear - 60 };
            default:
                throw new IllegalArgumentException("id는 10/20/30/40/50/60 중 하나여야 합니다.");
        }
    }
}
