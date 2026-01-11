package com.jeonlog.exhibition_recommender.recommendation.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class PopularExhibitionDto {
    private Long exhibitionId;
    private String title;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;

    private long recentClickCount;
    private long recentBookmarkCount;
    private double score;
}
