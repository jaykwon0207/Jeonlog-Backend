package com.jeonlog.exhibition_recommender.recommendation.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PopularByGenderRecommendationRepository extends JpaRepository<Exhibition, Long> {

    @Query("""
        SELECT e.id
        FROM Exhibition e
        LEFT JOIN ExhibitionClickLog ecl
               ON ecl.exhibition = e
              AND ecl.clickedDate BETWEEN :fromDate AND :toDate
              AND ecl.user.gender = :gender
        LEFT JOIN Bookmark b
               ON b.exhibition = e
              AND b.createdAt BETWEEN :fromDt AND :toDt
              AND b.user.gender = :gender
        WHERE e.endDate >= :today
        GROUP BY e.id
        ORDER BY (
            (COUNT(DISTINCT ecl.id) * 1.0) * :clickWeight
          + (COUNT(DISTINCT b.id)   * 1.0) * :bookmarkWeight
        ) DESC,
        COUNT(DISTINCT b.id) DESC, COUNT(DISTINCT ecl.id) DESC, e.id DESC
        """)
    List<Long> findTopPopularByGender(
            @Param("today") LocalDate today,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            @Param("gender") Gender gender,
            @Param("clickWeight") Double clickWeight,
            @Param("bookmarkWeight") Double bookmarkWeight,
            Pageable pageable
    );
}
