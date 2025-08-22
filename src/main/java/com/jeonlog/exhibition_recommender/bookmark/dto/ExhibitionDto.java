package com.jeonlog.exhibition_recommender.bookmark.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExhibitionDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String posterUrl;
    private String startDate;
    private String endDate;
    private Integer price;
    private String exhibitionMood;
    private VenueDto venue;

    @Getter
    @Builder
    public static class VenueDto {
        private Long id;
        private String name;
        private String type;
        private String address;
        private Double latitude;
        private Double longitude;
    }
}