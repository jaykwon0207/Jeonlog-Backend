package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionResponseDto {
    private Long id;
    private String title;
    private String location;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private int price;
    private String description;
    private String contact;
    private String website;
    private String viewingTime;

    private VenueInfo venue;

    public static ExhibitionResponseDto from(Exhibition exhibition) {
        return ExhibitionResponseDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .location(exhibition.getLocation())
                .posterUrl(exhibition.getPosterUrl())
                .startDate(exhibition.getStartDate())
                .endDate(exhibition.getEndDate())
                .price(exhibition.getPrice())
                .description(exhibition.getDescription())
                .contact(exhibition.getContact())
                .website(exhibition.getWebsite())
                .viewingTime(exhibition.getViewingTime())
                .venue(VenueInfo.from(exhibition.getVenue()))
                .build();
    }
}
