package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.*;
import lombok.*;

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
    private String location;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private int price;
    private boolean isFree;
    private String description;
    private String contact;
    private String website;
    private String viewingTime;

    // ✅ 장르(enum) 직접 출력
    private GenreType genre;

    // ✅ 전시 테마도 그대로 출력 (enum)
    private ExhibitionTheme exhibitionTheme;

    // ✅ 전시장 정보
    private VenueInfo venue;

    // ✅ 작가 리스트
    private List<ArtistInfo> artists;

    public static ExhibitionResponseDto from(Exhibition exhibition) {
        return ExhibitionResponseDto.builder()
                .id(exhibition.getId())
                .title(exhibition.getTitle())
                .location(exhibition.getLocation())
                .posterUrl(exhibition.getPosterUrl())
                .startDate(exhibition.getStartDate())
                .endDate(exhibition.getEndDate())
                .price(exhibition.getPrice())
                .isFree(exhibition.isFree())
                .description(exhibition.getDescription())
                .contact(exhibition.getContact())
                .website(exhibition.getWebsite())
                .viewingTime(exhibition.getViewingTime())
                .genre(exhibition.getGenre()) // ✅ DB의 genre 컬럼(enum) 그대로
                .exhibitionTheme(exhibition.getExhibitionTheme()) // ✅ theme도 그대로
                .venue(VenueInfo.from(exhibition.getVenue()))
                .artists(exhibition.getArtists().stream()
                        .map(ArtistInfo::from)
                        .collect(Collectors.toList()))
                .build();
    }
}