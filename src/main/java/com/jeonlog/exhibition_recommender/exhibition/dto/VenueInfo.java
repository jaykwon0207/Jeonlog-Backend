package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;

public record VenueInfo(
        Long id,
        String name,
        String website,
        String openingHours,
        String address
) {
    public static VenueInfo from(Venue venue) {
        return new VenueInfo(
                venue.getId(),
                venue.getName(),
                venue.getWebsite(),
                venue.getOpeningHours(),
                venue.getAddress()
        );
    }
}