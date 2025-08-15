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
    private String staticMapUrl;  // 정적 지도 이미지 URL
    private String naverMapUrl;   // 네이버 지도 연동 URL

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
                // BigDecimal → Double 변환 (null 처리 포함)
                .latitude(v.getLatitude() != null ? v.getLatitude().doubleValue() : null)
                .longitude(v.getLongitude() != null ? v.getLongitude().doubleValue() : null)
                .coverImageUrl(coverImageUrl)
                .staticMapUrl(v.getLatitude() != null && v.getLongitude() != null ?
                        generateStaticMapUrl(v.getLongitude().doubleValue(), v.getLatitude().doubleValue()) : null)
                .naverMapUrl(v.getLatitude() != null && v.getLongitude() != null ?
                        generateNaverMapUrl(v.getLongitude().doubleValue(), v.getLatitude().doubleValue(), v.getName()) : null)
                .build();
    }

    // 정적 지도 이미지 URL 생성 (기본 설정)
    private static String generateStaticMapUrl(Double longitude, Double latitude) {
        return String.format(
                "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w=400&h=300&center=%.6f,%.6f&level=15&maptype=nbasic&format=png&scale=1&markers=type:t|size:mid|pos:%.6f,%.6f|label:📍",
                longitude, latitude, longitude, latitude
        );
    }

    // 네이버 지도 연동 URL 생성
    private static String generateNaverMapUrl(Double longitude, Double latitude, String venueName) {
        return String.format(
                "https://map.naver.com/p/search/%s?c=%.6f,%.6f,15,0,0,0,dh",
                venueName, longitude, latitude
        );
    }
}
