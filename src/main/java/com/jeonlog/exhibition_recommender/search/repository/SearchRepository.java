package com.jeonlog.exhibition_recommender.search.repository;

import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchRepository extends JpaRepository<Search, Long> {

    @Query("""
            SELECT s.keyword AS keyword, COUNT(s) AS cnt
            FROM Search s
            WHERE (:from IS NULL OR s.searchedAt >= :from)
              AND (:to IS NULL OR s.searchedAt <= :to)
              AND s.keyword IS NOT NULL
              AND TRIM(s.keyword) <> ''
              AND (
                EXISTS (
                    SELECT 1 FROM Exhibition e
                    WHERE LOWER(e.title) LIKE CONCAT('%', LOWER(s.keyword), '%')
                )
                OR EXISTS (
                    SELECT 1 FROM Venue v
                    WHERE LOWER(v.name) LIKE CONCAT('%', LOWER(s.keyword), '%')
                )
                OR EXISTS (
                    SELECT 1 FROM Artist a
                    WHERE LOWER(a.name) LIKE CONCAT('%', LOWER(s.keyword), '%')
                )
              )
            GROUP BY s.keyword
            ORDER BY COUNT(s) DESC, s.keyword ASC
            """)
    List<Object[]> aggregateKeywordCounts(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);

    void deleteAllByUser(User user);

}
