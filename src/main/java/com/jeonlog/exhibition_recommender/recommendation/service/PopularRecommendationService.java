package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.recommendation.dto.PopularRecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.repository.PopularRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularRecommendationService {

    private static final int LIMIT = 20;   // 제공 전시 개수
    private static final int DAYS  = 30;   // 최근 30일

    private final PopularRecommendationRepository popularRepo;

    public List<PopularRecommendationDto> getPopular() {
        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusDays(DAYS);
        LocalDate to    = today;

        return popularRepo.findPopular(today, from, to, PageRequest.of(0, LIMIT));
    }
}
