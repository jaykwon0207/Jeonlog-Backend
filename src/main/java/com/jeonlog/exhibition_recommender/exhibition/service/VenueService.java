package com.jeonlog.exhibition_recommender.exhibition.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionStatus;
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

import java.time.Clock;
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
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found with id: " + id));

        Optional<VenuePhoto> cover = venuePhotoRepository
                .findFirstByVenue_IdAndIsCoverTrueOrderBySortOrderAscIdAsc(id);

        String coverUrl = cover.map(VenuePhoto::getImageUrl).orElse(null);
        return VenueDetailResponseDto.of(venue, coverUrl);
    }

    public List<VenuePhotoDto> getVenuePhotos(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new VenueNotFoundException("Venue not found with id: " + id);
        }
        return venuePhotoRepository
                .findTop20ByVenue_IdOrderBySortOrderAscIdAsc(id)
                .stream().map(VenuePhotoDto::from)
                .toList();
    }

    public Page<ExhibitionResponseDto> getVenueExhibitions(Long venueId,
                                                           ExhibitionStatus status,
                                                           Pageable pageable) {
        if (!venueRepository.existsById(venueId)) {
            throw new VenueNotFoundException("Venue not found with id: " + venueId);
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        Pageable resolved = applyDefaultSortIfAbsent(pageable, status);

        Page<Exhibition> page = switch (status) {
            case PAST ->
                    exhibitionRepository.findByVenue_IdAndEndDateBefore(venueId, today, resolved);
            case CURRENT ->
                    exhibitionRepository.findByVenue_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            venueId, today, today, resolved);
            case UPCOMING ->
                    exhibitionRepository.findByVenue_IdAndStartDateAfter(venueId, today, resolved);
        };

        return page.map(ExhibitionResponseDto::from);
    }

    /** 클라이언트가 정렬을 주지 않았을 때만 상태별 기본 정렬을 적용 */
    private Pageable applyDefaultSortIfAbsent(Pageable pageable, ExhibitionStatus status) {
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            return pageable;
        }
        Sort sort = switch (status) {
            case PAST     -> Sort.by(Sort.Direction.DESC, "endDate");
            case CURRENT  -> Sort.by(Sort.Direction.ASC,  "endDate");
            case UPCOMING -> Sort.by(Sort.Direction.ASC,  "startDate");
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}