package com.jeonlog.exhibition_recommender.map.service;

import com.jeonlog.exhibition_recommender.map.dto.GeocodingResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverMapService {

    private final WebClient webClient;
    
    @Value("${ncp.maps.key-id}")
    private String clientId;
    
    @Value("${ncp.maps.key}")
    private String clientSecret;
    
    @Value("${ncp.maps.base-url}")
    private String baseUrl;

    /**
     * 주소를 좌표로 변환 (Geocoding)
     * @param address 변환할 주소
     * @return GeocodingResponseDto
     */
    public Mono<GeocodingResponseDto> geocode(String address) {
        String url = baseUrl + "/map-geocode/v2/geocode";
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/map-geocode/v2/geocode")
                        .queryParam("query", address)
                        .build())
                .header("X-NCP-APIGW-API-KEY-ID", clientId)
                .header("X-NCP-APIGW-API-KEY", clientSecret)
                .retrieve()
                .bodyToMono(GeocodingResponseDto.class)
                .doOnError(error -> log.error("Geocoding API 호출 실패: {}", error.getMessage()))
                .doOnSuccess(response -> log.info("Geocoding 성공: {} -> {}개 결과", address, 
                        response.getAddresses() != null ? response.getAddresses().size() : 0));
    }



    /**
     * 주소를 좌표로 변환하여 간단한 결과만 반환
     * @param address 변환할 주소
     * @return 좌표 정보 (경도, 위도)
     */
    public Mono<Coordinates> getCoordinates(String address) {
        return geocode(address)
                .map(response -> {
                    if (response.getAddresses() != null && !response.getAddresses().isEmpty()) {
                        GeocodingResponseDto.Address firstAddress = response.getAddresses().get(0);
                        return new Coordinates(
                                Double.parseDouble(firstAddress.getX()),
                                Double.parseDouble(firstAddress.getY())
                        );
                    }
                    return null;
                });
    }

    /**
     * 정적 지도 이미지 URL 생성
     * @param longitude 경도
     * @param latitude 위도
     * @param zoom 줌 레벨 (1-20)
     * @param size 이미지 크기 (예: 400x300)
     * @return 정적 지도 이미지 URL
     */
    public String generateStaticMapUrl(double longitude, double latitude, int zoom, String size) {
        // Naver Static Map API URL 생성
        // https://naveropenapi.apigw.ntruss.com/map-static/v2/raster
        return String.format(
            "%s/map-static/v2/raster?w=%s&h=%s&center=%.6f,%.6f&level=%d&maptype=nbasic&format=png&scale=1&markers=type:t|size:mid|pos:%.6f,%.6f|label:%s",
            baseUrl,
            size.split("x")[0],
            size.split("x")[1],
            longitude, latitude,
            zoom,
            longitude, latitude,
            "📍"
        );
    }

    /**
     * 네이버 지도 연동 URL 생성
     * @param longitude 경도
     * @param latitude 위도
     * @param venueName 전시장 이름
     * @return 네이버 지도 URL
     */
    public String generateNaverMapUrl(double longitude, double latitude, String venueName) {
        // 네이버 지도 웹/앱 연동 URL
        // 웹: https://map.naver.com/p/search/장소명?c=경도,위도,줌레벨
        // 앱: nvmap://search?query=장소명&lat=위도&lng=경도
        
        // 웹용 URL (모바일에서도 네이버 지도 앱으로 자동 연결)
        return String.format(
            "https://map.naver.com/p/search/%s?c=%.6f,%.6f,15,0,0,0,dh",
            venueName, longitude, latitude
        );
    }

    /**
     * 좌표 정보를 담는 내부 클래스
     */
    public static class Coordinates {
        private final double longitude;
        private final double latitude;

        public Coordinates(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public double getLongitude() { return longitude; }
        public double getLatitude() { return latitude; }
    }
}
