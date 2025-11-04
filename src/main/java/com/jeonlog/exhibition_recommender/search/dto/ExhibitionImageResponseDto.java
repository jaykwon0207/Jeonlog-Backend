package com.jeonlog.exhibition_recommender.search.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionResponseDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private int price;
    private boolean isFree;
    private ExhibitionTheme exhibitionTheme;
    private GenreType genre;
    private String contact;
    private String website;
    private String viewingTime;

    // 추가 예시: 작가명 리스트, 전시장 이름
    private List<String> artistNames;
    private String venueName;

    public static ExhibitionImageResponseDto from(Exhibition exhibition) {
        return ExhibitionImageResponseDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .description(exhibition.getDescription())
                .location(exhibition.getLocation())
                .posterUrl(exhibition.getPosterUrl())
                .startDate(exhibition.getStartDate())
                .endDate(exhibition.getEndDate())
                .price(exhibition.getPrice())
                .isFree(exhibition.isFree())
                .exhibitionTheme(exhibition.getExhibitionTheme())
                .genre(exhibition.getGenre())
                .contact(exhibition.getContact())
                .website(exhibition.getWebsite())
                .viewingTime(exhibition.getViewingTime())
                .artistNames(exhibition.getArtists().stream()
                        .map(a -> a.getName())
                        .collect(Collectors.toList()))
                .venueName(exhibition.getVenue().getName())
                .build();
    }
}
