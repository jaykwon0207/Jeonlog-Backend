package com.jeonlog.exhibition_recommender.bookmark.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserBookmarkDto {
    private Long id;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String posterUrl;

    public static UserBookmarkDto from(Exhibition exhibition) {
        return UserBookmarkDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .location(exhibition.getLocation())
                .startDate(exhibition.getStartDate())
                .endDate(exhibition.getEndDate())
                .posterUrl(exhibition.getPosterUrl())
                .build();
    }
}
