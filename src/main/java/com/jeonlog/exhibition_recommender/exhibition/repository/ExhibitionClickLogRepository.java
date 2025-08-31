package com.jeonlog.exhibition_recommender.exhibition.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionClickLog;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExhibitionClickLogRepository extends JpaRepository<ExhibitionClickLog,Long> {
    Optional<ExhibitionClickLog> findByUserAndExhibitionAndClickedDate(User user, Exhibition exhibition, LocalDate clickedDate);



   // 특정 전시회의 연령대별 클릭 수
    @Query("SELECT " +
            "CASE " +
            "  WHEN (YEAR(CURRENT_DATE) - u.birthYear) < 20 THEN '10대' " +
            "  WHEN (YEAR(CURRENT_DATE) - u.birthYear) < 30 THEN '20대' " +
            "  WHEN (YEAR(CURRENT_DATE) - u.birthYear) < 40 THEN '30대' " +
            "  WHEN (YEAR(CURRENT_DATE) - u.birthYear) < 50 THEN '40대' " +
            "  ELSE '50대 이상' " +
            "END as ageGroup, " +
            "COUNT(ecl) as clickCount " +
            "FROM ExhibitionClickLog ecl " +
            "JOIN ecl.exhibition e " +
            "JOIN ecl.user u " +
            "WHERE e.id = :exhibitionId " +
            "GROUP BY ageGroup " +
            "ORDER BY clickCount DESC")
    List<Object[]> findClickStatsByAgeGroupForExhibition(@Param("exhibitionId") Long exhibitionId);


    // 특정 전시회의 성별별 클릭 수
    @Query("SELECT u.gender, COUNT(ecl) as clickCount " +
           "FROM ExhibitionClickLog ecl " +
           "JOIN ecl.user u " +
           "WHERE ecl.exhibition.id = :exhibitionId AND u.gender IS NOT NULL " +
           "GROUP BY u.gender")
    List<Object[]> findClickStatsByGenderForExhibition(@Param("exhibitionId") Long exhibitionId);
}
