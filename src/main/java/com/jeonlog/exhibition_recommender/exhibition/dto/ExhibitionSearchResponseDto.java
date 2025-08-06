package com.jeonlog.exhibition_recommender.exhibition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionSearchResponseDto {
    private Long id;
    private String title;
    private String artist;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String posterUrl;
    private int price;
}
