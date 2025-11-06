package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueInfo {
    private Long id;
    private String name;
    private String address;

    public static VenueInfo from(Venue venue) {
        return VenueInfo.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .build();
    }
}