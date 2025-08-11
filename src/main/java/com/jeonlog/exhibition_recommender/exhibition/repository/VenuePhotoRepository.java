package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.VenuePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenuePhotoRepository extends JpaRepository<VenuePhoto, Long> {

    // 사진 개수 (20장 제한 검사용)
    long countByVenue_Id(Long venueId);

    // 정렬된 상위 20장 조회 (GET 용)
    List<VenuePhoto> findTop20ByVenue_IdOrderBySortOrderAscIdAsc(Long venueId);

    // 전부 조회가 필요할 때
    List<VenuePhoto> findAllByVenue_IdOrderBySortOrderAscIdAsc(Long venueId);

    // 대표 이미지 1장
    VenuePhoto findFirstByVenue_IdAndIsCoverTrueOrderBySortOrderAscIdAsc(Long venueId);

    // 장소 삭제 시 사진 일괄 삭제가 필요하면
    void deleteByVenue_Id(Long venueId);
}
