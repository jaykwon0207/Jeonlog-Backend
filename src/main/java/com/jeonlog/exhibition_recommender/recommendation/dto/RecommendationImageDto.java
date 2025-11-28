package com.jeonlog.exhibition_recommender.recommendation.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.Builder;

@Builder
public record RecommendationImageDto(
        Long id,
        String posterUrl,
        String personalizedPosterUrl
) {
    public static RecommendationImageDto from(Exhibition e) {
        return RecommendationImageDto.builder()
                .id(e.getId())
                .posterUrl(e.getPosterUrl())
                .personalizedPosterUrl(e.getPersonalizedPosterUrl())
                .build();
    }
}