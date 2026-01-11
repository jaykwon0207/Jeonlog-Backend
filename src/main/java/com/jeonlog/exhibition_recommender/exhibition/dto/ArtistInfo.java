package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Artist;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistInfo {
    private Long id;
    private String name;

    public static ArtistInfo from(Artist artist) {
        return ArtistInfo.builder()
                .id(artist.getId())
                .name(artist.getName())
                .build();
    }
}