package com.jeonlog.exhibition_recommender.recommendation.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RecommendationDto(
        Long id,
        String title,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        String posterUrl,
        String generalRecommendationsPosterUrl,
        String personalizedPosterUrl
) {
    public static RecommendationDto from(Exhibition e) {
        return RecommendationDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .location(e.getLocation())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .posterUrl(e.getPosterUrl())
                .generalRecommendationsPosterUrl(e.getGeneralRecommendationsPosterUrl())
                .personalizedPosterUrl(e.getPersonalizedPosterUrl())
                .build();
    }
}