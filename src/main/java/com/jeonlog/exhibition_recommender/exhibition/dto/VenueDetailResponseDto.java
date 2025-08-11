package com.jeonlog.exhibition_recommender.exhibition.dto;

import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VenueDetailResponseDto implements Serializable {
    private Long id;
    private String name;
    private String type;          // enum -> String 변환
    private String description;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String openingHours;
    private Double latitude;
    private Double longitude;
    private String coverImageUrl; // 대표 이미지(없으면 null)

    public static VenueDetailResponseDto of(Venue v, String coverImageUrl) {
        return VenueDetailResponseDto.builder()
                .id(v.getId())
                .name(v.getName())
                .type(v.getType() != null ? v.getType().name() : null)
                .description(v.getDescription())
                .address(v.getAddress())
                .phone(v.getPhone())
                .email(v.getEmail())
                .website(v.getWebsite())
                .openingHours(v.getOpeningHours())
                .latitude(v.getLatitude())
                .longitude(v.getLongitude())
                .coverImageUrl(coverImageUrl)
                .build();
    }
}
