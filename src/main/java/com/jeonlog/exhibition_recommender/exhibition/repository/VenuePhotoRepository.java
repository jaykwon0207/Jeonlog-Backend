package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.VenuePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenuePhotoRepository extends JpaRepository<VenuePhoto, Long> {

    long countByVenue_Id(Long venueId);

    List<VenuePhoto> findTop20ByVenue_IdOrderBySortOrderAscIdAsc(Long venueId);

    List<VenuePhoto> findAllByVenue_IdOrderBySortOrderAscIdAsc(Long venueId);

    Optional<VenuePhoto> findFirstByVenue_IdAndIsCoverTrueOrderBySortOrderAscIdAsc(Long venueId);

    void deleteByVenue_Id(Long venueId);
}
