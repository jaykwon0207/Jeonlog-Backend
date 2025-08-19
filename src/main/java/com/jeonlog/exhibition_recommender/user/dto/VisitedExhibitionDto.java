package com.jeonlog.exhibition_recommender.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisitedExhibitionDto {
    private Long id;
    private String title;
    private String location;
    private String startDate;
    private String endDate;
    private String posterUrl;
}
