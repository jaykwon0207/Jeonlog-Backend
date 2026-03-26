package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
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
        ORDER BY e.endDate ASC, e.startDate DESC, e.id DESC
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
        ORDER BY e.endDate ASC, e.startDate DESC, e.id DESC
        """)
    List<Exhibition> findActiveByGenreInExcluding(
            @Param("today") LocalDate today,
            @Param("genres") Collection<GenreType> genres,
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.startDate <= :today AND e.endDate >= :today
          AND e.id NOT IN :excludeIds
        ORDER BY e.endDate ASC, e.startDate DESC, e.id DESC
        """)
    List<Exhibition> findActiveExcluding(
            @Param("today") LocalDate today,
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.startDate > :today AND e.startDate <= :until
          AND e.id NOT IN :excludeIds
        ORDER BY e.startDate ASC, e.endDate ASC, e.id DESC
        """)
    List<Exhibition> findUpcomingExcluding(
            @Param("today") LocalDate today,
            @Param("until") LocalDate until,
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.endDate >= :today
          AND e.id NOT IN :excludeIds
        ORDER BY e.endDate ASC, e.startDate DESC, e.id DESC
        """)
    List<Exhibition> findAnyOpenExcluding(
            @Param("today") LocalDate today,
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.id NOT IN :excludeIds
        ORDER BY e.id DESC
        """)
    List<Exhibition> findAnyExcluding(
            @Param("excludeIds") Collection<Long> excludeIds,
            Pageable pageable
    );

    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleContainingIgnoreCase(String titlePart);

    // 전시 정보 조회 시 venue 정보도 함께 조회
    @Query("SELECT e FROM Exhibition e JOIN FETCH e.venue")
    List<Exhibition> findAllWithVenue();

    // 전시 종료 2주전 알림을 위해 추가
    List<Exhibition> findByEndDate(LocalDate endDate);

}
