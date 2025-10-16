package com.jeonlog.exhibition_recommender.search.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionImageResponseDto {
    private Long id;
    private String posterUrl;

    public static ExhibitionImageResponseDto from(Exhibition exhibition) {
        return ExhibitionImageResponseDto.builder()
                .id(exhibition.getId())
                .posterUrl(exhibition.getPosterUrl())
                .build();
    }
}
