package com.jeonlog.exhibition_recommender.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecommendedKeywordDto {
    private final String keyword;
    private final KeywordType type;
    private final double score;
}
