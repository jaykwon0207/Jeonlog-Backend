package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionMood;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionDetailResponseDto {

    private Long id;
    private String title;
    private String description;
    private String location;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int price;
    private boolean isFree;
    private ExhibitionMood exhibitionMood;
    private List<String> artists;
    private List<String> genres;

    public static ExhibitionDetailResponseDto from(Exhibition exhibition) {
        List<String> artistNames = exhibition.getArtists() != null 
                ? exhibition.getArtists().stream()
                        .map(artist -> artist.getName())
                        .collect(Collectors.toList())
                : List.of();

        List<String> genreNames = exhibition.getExhibitionGenres() != null
                ? exhibition.getExhibitionGenres().stream()
                        .map(exhibitionGenre -> exhibitionGenre.getGenre().getGenreType().name())
                        .collect(Collectors.toList())
                : List.of();

        return ExhibitionDetailResponseDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .description(exhibition.getDescription())
                .location(exhibition.getLocation())
                .posterUrl(exhibition.getPosterUrl())
                .startDate(exhibition.getStartDate())
                .endDate(exhibition.getEndDate())
                .latitude(exhibition.getLatitude())
                .longitude(exhibition.getLongitude())
                .price(exhibition.getPrice())
                .isFree(exhibition.isFree())
                .exhibitionMood(exhibition.getExhibitionMood())
                .artists(artistNames)
                .genres(genreNames)
                .build();
    }
}
