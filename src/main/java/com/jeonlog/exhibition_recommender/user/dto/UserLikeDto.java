package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserLikeDto {

    private Long id;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String posterUrl;

    public static UserLikeDto from(Exhibition exhibition) {
        return UserLikeDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .location(exhibition.getLocation())
                .startDate(exhibition.getStartDate())
                .endDate(exhibition.getEndDate())
                .posterUrl(exhibition.getPosterUrl())
                .build();
    }
}
