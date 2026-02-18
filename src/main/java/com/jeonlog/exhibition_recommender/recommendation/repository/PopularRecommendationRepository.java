package com.jeonlog.exhibition_recommender.recommendation.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PopularRecommendationRepository extends JpaRepository<Exhibition, Long> {

    @Query("""
        SELECT e.id
        FROM Exhibition e
        LEFT JOIN ExhibitionClickLog ecl
               ON ecl.exhibition = e
              AND ecl.clickedDate BETWEEN :fromDate AND :toDate
        LEFT JOIN Bookmark b
               ON b.exhibition = e
              AND b.createdAt BETWEEN :fromDt AND :toDt
        WHERE e.endDate >= :today
        GROUP BY e.id
        ORDER BY (
            COUNT(ecl) * :clickWeight
          + COUNT(b)  * :bookmarkWeight
        ) DESC,
        COUNT(b) DESC,
        COUNT(ecl) DESC,
        e.id DESC
        """)
    List<Long> findTopPopularExhibitionIds(
            @Param("today") LocalDate today,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            @Param("clickWeight") double clickWeight,
            @Param("bookmarkWeight") double bookmarkWeight,
            org.springframework.data.domain.Pageable pageable
    );
}
