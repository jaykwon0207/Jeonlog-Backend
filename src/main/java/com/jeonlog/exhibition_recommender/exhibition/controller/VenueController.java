package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionStatus;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenueDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenueListResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenuePhotoDto;
import com.jeonlog.exhibition_recommender.exhibition.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping // 전체 venue 조회
    public ResponseEntity<ApiResponse<Page<VenueListResponseDto>>> getAllVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<VenueListResponseDto> venues = venueService.getAllVenues(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(venues));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<VenueListResponseDto>>> searchVenues(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<VenueListResponseDto> venues = venueService.searchVenues(query, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(venues));
    }

    @GetMapping("/{id}") // 전시회 조회
    public ResponseEntity<ApiResponse<VenueDetailResponseDto>> getVenue(@PathVariable Long id) {
        VenueDetailResponseDto venue = venueService.getVenueDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(venue));
    }

    @GetMapping("/{id}/photos")
    public ResponseEntity<ApiResponse<List<VenuePhotoDto>>> getVenuePhotos(@PathVariable Long id) {
        List<VenuePhotoDto> photos = venueService.getVenuePhotos(id);
        return ResponseEntity.ok(ApiResponse.ok(photos));
    }

    @GetMapping("/{id}/exhibitions") // 상태별 전시 목록
    public ResponseEntity<ApiResponse<Page<ExhibitionResponseDto>>> getVenueExhibitions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "current") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ExhibitionStatus resolvedStatus = ExhibitionStatus.valueOf(status.toUpperCase());
        Page<ExhibitionResponseDto> exhibitions =
                venueService.getVenueExhibitions(id, resolvedStatus, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(exhibitions));
    }
}
