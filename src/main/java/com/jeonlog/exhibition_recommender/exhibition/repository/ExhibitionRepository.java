package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    // 과거 전시
    Page<Exhibition> findByVenue_IdAndEndDateBefore(Long venueId, LocalDate today, Pageable pageable);

    // 현재 전시
    Page<Exhibition> findByVenue_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long venueId, LocalDate today1, LocalDate today2, Pageable pageable);

    // 예정 전시
    Page<Exhibition> findByVenue_IdAndStartDateAfter(Long venueId, LocalDate today, Pageable pageable);

}
