package com.jeonlog.exhibition_recommender.recommendation.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.recommendation.domain.InitialExhibition;
import lombok.Builder;

@Builder
public record InitialExhibitionDto(
        Long id,
        String name,
        String description,
        String posterUrl,
        GenreType genre,
        ExhibitionTheme exhibitionTheme
) {
    public static InitialExhibitionDto from(InitialExhibition e) {
        return InitialExhibitionDto.builder()
                .id(e.getId())
                .name(e.getName())
                .posterUrl(e.getPosterUrl())
                .genre(e.getGenre())
                .exhibitionTheme(e.getExhibitionTheme())
                .build();
    }
}
