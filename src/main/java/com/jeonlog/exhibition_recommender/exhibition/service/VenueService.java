// src/main/java/com/jeonlog/exhibition_recommender/exhibition/service/VenueService.java
package com.jeonlog.exhibition_recommender.exhibition.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.domain.VenuePhoto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenueDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenuePhotoDto;
import com.jeonlog.exhibition_recommender.exhibition.exception.VenueNotFoundException;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenuePhotoRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenuePhotoRepository venuePhotoRepository;
    private final ExhibitionRepository exhibitionRepository;

    public VenueDetailResponseDto getVenueDetail(Long id) {
        Venue v = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found with id: " + id));

        String coverUrl = Optional.ofNullable(
                venuePhotoRepository.findFirstByVenue_IdAndIsCoverTrueOrderBySortOrderAscIdAsc(id)
        ).map(VenuePhoto::getImageUrl).orElse(null);

        return VenueDetailResponseDto.of(v, coverUrl);
    }

    public List<VenuePhotoDto> getVenuePhotos(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new VenueNotFoundException("Venue not found with id: " + id);
        }
        return venuePhotoRepository
                .findTop20ByVenue_IdOrderBySortOrderAscIdAsc(id)
                .stream().map(VenuePhotoDto::from).toList();
    }

    // 상태별 전시 목록
    public Page<ExhibitionResponseDto> getVenueExhibitions(Long venueId, String status, int page, int size) {
        if (!venueRepository.existsById(venueId)) {
            throw new VenueNotFoundException("Venue not found with id: " + venueId);
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Pageable pageable = switch (status.toLowerCase()) {
            case "past"     -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "endDate"));
            case "current"  -> PageRequest.of(page, size, Sort.by(Sort.Direction.ASC,  "endDate"));
            case "upcoming" -> PageRequest.of(page, size, Sort.by(Sort.Direction.ASC,  "startDate"));
            default -> throw new IllegalArgumentException("status must be one of past|current|upcoming");
        };

        Page<Exhibition> result = switch (status.toLowerCase()) {
            case "past" -> exhibitionRepository.findByVenue_IdAndEndDateBefore(venueId, today, pageable);
            case "current" -> exhibitionRepository
                    .findByVenue_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(venueId, today, today, pageable);
            case "upcoming" -> exhibitionRepository.findByVenue_IdAndStartDateAfter(venueId, today, pageable);
            default -> Page.empty(pageable);
        };

        return result.map(ExhibitionResponseDto::from);
    }
}
