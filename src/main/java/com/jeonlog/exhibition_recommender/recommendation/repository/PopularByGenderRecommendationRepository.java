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
        GROUP BY e.id
        ORDER BY (
            (COUNT(ecl) * 1.0) * :clickWeight
          + (COUNT(b)   * 1.0) * :bookmarkWeight
        ) DESC,
        COUNT(b) DESC, COUNT(ecl) DESC, e.id DESC
        """)
    List<Long> findTopPopularByGender(
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
