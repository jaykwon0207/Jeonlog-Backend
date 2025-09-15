package com.jeonlog.exhibition_recommender.recommendation.repository;

import com.jeonlog.exhibition_recommender.recommendation.dto.PopularRecommendationDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Repository
public interface PopularRecommendationRepository extends Repository<com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition, Long> {

    @Query("""
        SELECT new com.jeonlog.exhibition_recommender.recommendation.dto.PopularRecommendationDto(
            e.id, e.title, e.location, e.startDate, e.endDate, e.posterUrl,
            ( COALESCE(COUNT(DISTINCT ecl.id), 0)
            + COALESCE(COUNT(DISTINCT rl.id),  0) ) * 1.0
        )
        FROM Exhibition e
        LEFT JOIN ExhibitionClickLog ecl
               ON ecl.exhibition = e
              AND ecl.clickedDate BETWEEN :from AND :to
        LEFT JOIN ExhibitionRecord er
               ON er.exhibition = e
        LEFT JOIN RecordLike rl
               ON rl.record = er
              AND rl.likedAt BETWEEN :from AND :to
        WHERE e.startDate <= :today
          AND e.endDate   >= :today
        GROUP BY e.id, e.title, e.location, e.startDate, e.endDate, e.posterUrl
        ORDER BY ( COALESCE(COUNT(DISTINCT ecl.id), 0)
                 + COALESCE(COUNT(DISTINCT rl.id),  0) ) DESC,
                 e.id DESC
        """)
    List<PopularRecommendationDto> findPopular(
            @Param("today") LocalDate today,
            @Param("from")  LocalDate from,
            @Param("to")    LocalDate to,
            Pageable pageable
    );
}
