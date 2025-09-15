package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    // 과거 전시
    Page<Exhibition> findByVenue_IdAndEndDateBefore(Long venueId, LocalDate today, Pageable pageable);

    // 현재 전시
    Page<Exhibition> findByVenue_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long venueId, LocalDate today1, LocalDate today2, Pageable pageable);

    // 예정 전시
    Page<Exhibition> findByVenue_IdAndStartDateAfter(Long venueId, LocalDate today, Pageable pageable);

    // 전시추천: 진행 중 & 교집합(장르∩분위기)
    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.startDate <= :today AND e.endDate >= :today
          AND e.genre IN :genres
          AND e.exhibitionTheme IN :moods
          AND e.id NOT IN :excludeIds
        """)
    List<Exhibition> findActiveByGenreInAndMoodInExcluding(
            @Param("today") LocalDate today,
            @Param("genres") Collection<GenreType> genres,
            @Param("moods") Collection<ExhibitionTheme> moods,
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    // 전시추천: 진행 중 & 장르만
    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.startDate <= :today AND e.endDate >= :today
          AND e.genre IN :genres
          AND e.id NOT IN :excludeIds
        """)
    List<Exhibition> findActiveByGenreInExcluding(
            @Param("today") LocalDate today,
            @Param("genres") Collection<GenreType> genres,
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    // 전시추천: 진행 중 랜덤 보충
    @Query(value = """
        SELECT *
        FROM exhibitions e
        WHERE e.start_date <= :today AND e.end_date >= :today
          AND e.id NOT IN (:excludeIds)
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Exhibition> pickActiveRandomExcluding(
            @Param("today") LocalDate today,
            @Param("excludeIds") Collection<Long> excludeIds,
            @Param("limit") int limit
    );

    // 전시추천: 임박 예정(오늘~+60일) 랜덤 보충
    @Query(value = """
        SELECT *
        FROM exhibitions e
        WHERE e.start_date > :today AND e.start_date <= :until
          AND e.id NOT IN (:excludeIds)
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Exhibition> pickUpcomingRandomExcluding(
            @Param("today") LocalDate today,
            @Param("until") LocalDate until,
            @Param("excludeIds") Collection<Long> excludeIds,
            @Param("limit") int limit
    );

    // 전시추천: 전 범위 랜덤 보충
    @Query(value = """
        SELECT *
        FROM exhibitions e
        WHERE e.id NOT IN (:excludeIds)
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Exhibition> pickAnyRandomExcluding(
            @Param("excludeIds") Collection<Long> excludeIds,
            @Param("limit") int limit
    );

    //인기 전시 추천
    @Query("""
        SELECT e
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
        GROUP BY e
        ORDER BY ( COUNT(DISTINCT ecl.id) + COUNT(DISTINCT rl.id) ) DESC, e.id DESC
        """)
    List<Exhibition> findPopularByClicksAndLikes1to1(
            @Param("today") LocalDate today,
            @Param("from")  LocalDate from,
            @Param("to")    LocalDate to,
            Pageable pageable
    );

    //특정 연령대 전시 추천
    @Query("""
    SELECT e
    FROM Exhibition e
    LEFT JOIN ExhibitionClickLog ecl
           ON ecl.exhibition = e
          AND ecl.clickedDate BETWEEN :from AND :to
    LEFT JOIN ecl.user uc
    LEFT JOIN ExhibitionRecord er
           ON er.exhibition = e
    LEFT JOIN RecordLike rl
           ON rl.record = er
          AND rl.likedAt BETWEEN :from AND :to
    LEFT JOIN rl.user ul
    WHERE e.startDate <= :today
      AND e.endDate   >= :today
    GROUP BY e
    ORDER BY
      (
        SUM(CASE
              WHEN uc.birthYear BETWEEN :minBirthYear AND :maxBirthYear
              THEN 1 ELSE 0
            END)
        +
        SUM(CASE
              WHEN ul.birthYear BETWEEN :minBirthYear AND :maxBirthYear
              THEN 1 ELSE 0
            END)
      ) DESC,
      e.id DESC
    """)
    List<Exhibition> findPopularByAgeBand(
            @Param("today") LocalDate today,
            @Param("from")  LocalDate from,
            @Param("to")    LocalDate to,
            @Param("minBirthYear") int minBirthYear,
            @Param("maxBirthYear") int maxBirthYear,
            Pageable pageable
    );

    //성별 별  전시 추천
    @Query("""
        SELECT e
        FROM Exhibition e
        LEFT JOIN ExhibitionClickLog ecl
               ON ecl.exhibition = e
              AND ecl.clickedDate BETWEEN :from AND :to
        LEFT JOIN ecl.user uc
        LEFT JOIN ExhibitionRecord er
               ON er.exhibition = e
        LEFT JOIN RecordLike rl
               ON rl.record = er
              AND rl.likedAt BETWEEN :from AND :to
        LEFT JOIN rl.user ul
        WHERE e.startDate <= :today
          AND e.endDate   >= :today
        GROUP BY e
        ORDER BY
          (
            SUM(CASE WHEN uc.gender = :gender THEN 1 ELSE 0 END)
            +
            SUM(CASE WHEN ul.gender = :gender THEN 1 ELSE 0 END)
          ) DESC,
          e.id DESC
        """)
    List<Exhibition> findPopularByGender(
            @Param("today")   java.time.LocalDate today,
            @Param("from")    java.time.LocalDate from,
            @Param("to")      java.time.LocalDate to,
            @Param("gender") Gender gender,
            Pageable pageable
    );
}
