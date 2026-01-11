package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionGenre;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionGenreInfo {
    private Long id;            // ExhibitionGenre ID
    private String genreType;   // Enum 이름(String으로 변환)

    public static ExhibitionGenreInfo from(ExhibitionGenre exhibitionGenre) {
        return ExhibitionGenreInfo.builder()
                .id(exhibitionGenre.getId())
                .genreType(
                        exhibitionGenre.getGenre() != null && exhibitionGenre.getGenre().getGenreType() != null
                                ? exhibitionGenre.getGenre().getGenreType().name()
                                : null
                )
                .build();
    }
}