package com.jeonlog.exhibition_recommender.exhibition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class ClickLogCreateResponse {
    private Long logId;
    private Long exhibitionId;
    private Long userId;
    private String clickedAt; // ISO-8601 문자열 (Instant.toString())
}
