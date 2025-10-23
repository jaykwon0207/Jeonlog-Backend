package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VenueListResponseDto implements Serializable {
    private Long id;
    private String name;
    private String type;          // enum -> String 변환
    private String address;
    private String coverImageUrl; // 대표 이미지(없으면 null)
    private String logoImageUrl;  // 로고 이미지(없으면 null)

    public static VenueListResponseDto from(Venue venue, String coverImageUrl) {
        return VenueListResponseDto.builder()
                .id(venue.getId())
                .name(venue.getName())
                .type(venue.getType() != null ? venue.getType().name() : null)
                .address(venue.getAddress())
                .coverImageUrl(coverImageUrl)
                .logoImageUrl(venue.getLogoImageUrl())
                .build();
    }
}
