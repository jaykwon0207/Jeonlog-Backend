package com.jeonlog.exhibition_recommender.map.controller;

import com.jeonlog.exhibition_recommender.map.dto.GeocodingResponseDto;
import com.jeonlog.exhibition_recommender.map.service.NaverMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapApiController {

    private final NaverMapService naverMapService;

    
      //주소를 좌표로 변환 (Geocoding)
      //GET /api/map/geocode?address=서울시 강남구
     
    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String address) {
        try {
            GeocodingResponseDto response = naverMapService.geocode(address).block();
            if (response != null && "OK".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", 
                        response != null ? response.getErrorMessage() : "Geocoding 실패"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Geocoding 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }



    
     // 주소를 좌표로 변환하여 간단한 결과만 반환
     // GET /api/map/coordinates?address=서울시 강남구
     
    @GetMapping("/coordinates")
    public ResponseEntity<?> getCoordinates(@RequestParam String address) {
        try {
            NaverMapService.Coordinates coordinates = naverMapService.getCoordinates(address).block();
            if (coordinates != null) {
                return ResponseEntity.ok(Map.of(
                        "longitude", coordinates.getLongitude(),
                        "latitude", coordinates.getLatitude()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "좌표를 찾을 수 없습니다"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "좌표 변환 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    
     // 정적 지도 이미지 URL 생성
     // GET /api/map/static-map?longitude=127.123&latitude=37.456&zoom=15&size=400x300
     
    @GetMapping("/static-map")
    public ResponseEntity<?> getStaticMap(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "15") int zoom,
            @RequestParam(defaultValue = "400x300") String size) {
        try {
            String staticMapUrl = naverMapService.generateStaticMapUrl(longitude, latitude, zoom, size);
            return ResponseEntity.ok(Map.of("staticMapUrl", staticMapUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "정적 지도 URL 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    
     // 네이버 지도 연동 URL 생성
     // GET /api/map/map-link?longitude=127.123&latitude=37.456&venueName=국립중앙박물관
     
    @GetMapping("/map-link")
    public ResponseEntity<?> getMapLink(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam String venueName) {
        try {
            String mapUrl = naverMapService.generateNaverMapUrl(longitude, latitude, venueName);
            return ResponseEntity.ok(Map.of("mapUrl", mapUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "지도 링크 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
