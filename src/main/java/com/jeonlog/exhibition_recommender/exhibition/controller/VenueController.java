// src/main/java/com/jeonlog/exhibition_recommender/exhibition/controller/VenueController.java
package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenueDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenuePhotoDto;
import com.jeonlog.exhibition_recommender.exhibition.exception.VenueNotFoundException;
import com.jeonlog.exhibition_recommender.exhibition.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getVenue(@PathVariable Long id) {
        try {
            VenueDetailResponseDto venue = venueService.getVenueDetail(id);
            return ResponseEntity.ok(venue);
        } catch (VenueNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "전시장 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{id}/photos")
    public ResponseEntity<?> getVenuePhotos(@PathVariable Long id) {
        try {
            List<VenuePhotoDto> photos = venueService.getVenuePhotos(id);
            return ResponseEntity.ok(photos);
        } catch (VenueNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "전시장 사진 조회 중 오류가 발생했습니다."));
        }
    }

    // ★ 상태별 전시 목록 (현재/예정/과거)
    @GetMapping("/{id}/exhibitions")
    public ResponseEntity<?> getVenueExhibitions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "current") String status, // current|upcoming|past
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<ExhibitionResponseDto> exhibitions = venueService.getVenueExhibitions(id, status, page, size);
            return ResponseEntity.ok(exhibitions);
        } catch (VenueNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "전시 목록 조회 중 오류가 발생했습니다."));
        }
    }
}
