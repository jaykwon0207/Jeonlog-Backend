package com.jeonlog.exhibition_recommender.recommendation.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PopularByAgeRecommendationRepository extends JpaRepository<Exhibition, Long> {

    @Query("""
        SELECT e.id
        FROM Exhibition e
        LEFT JOIN ExhibitionClickLog ecl
               ON ecl.exhibition = e
              AND ecl.clickedDate BETWEEN :fromDate AND :toDate
              AND ecl.user.birthYear BETWEEN :minBirthYear AND :maxBirthYear
        LEFT JOIN Bookmark b
               ON b.exhibition = e
              AND b.createdAt BETWEEN :fromDt AND :toDt
              AND b.user.birthYear BETWEEN :minBirthYear AND :maxBirthYear
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
    List<Long> findTopPopularByAge(
            @Param("today") LocalDate today,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("fromDt") LocalDateTime fromDt,
            @Param("toDt") LocalDateTime toDt,
            @Param("minBirthYear") Integer minBirthYear,
            @Param("maxBirthYear") Integer maxBirthYear,
            @Param("clickWeight") Double clickWeight,
            @Param("bookmarkWeight") Double bookmarkWeight,
            Pageable pageable
    );
}
