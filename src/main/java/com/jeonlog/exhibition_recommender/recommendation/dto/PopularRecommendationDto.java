package com.jeonlog.exhibition_recommender.recommendation.dto;

import java.time.LocalDate;

public record PopularRecommendationDto(
        Long id,
        String title,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        String posterUrl,
        double popularityScore
) {}
