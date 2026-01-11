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

    @Transactional(readOnly = true)
    public List<RecommendationDto> getPopular(int days, double clickWeight, double bookmarkWeight) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days);
        LocalDate toDate = today;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromDt = now.minusDays(days);
        LocalDateTime toDt = now;

        var top10 = org.springframework.data.domain.PageRequest.of(0, 10);

        List<Long> ids = popularRepository.findTopPopularExhibitionIds(
                fromDate, toDate, fromDt, toDt, clickWeight, bookmarkWeight, top10);

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

}

