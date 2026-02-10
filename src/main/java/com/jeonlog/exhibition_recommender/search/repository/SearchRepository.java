package com.jeonlog.exhibition_recommender.search.repository;

import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchRepository extends JpaRepository<Search, Long> {

    @Query("SELECT s.keyword AS keyword, COUNT(s) AS cnt " +
            "FROM Search s " +
            "WHERE (:from IS NULL OR s.searchedAt >= :from) " +
            "AND (:to IS NULL OR s.searchedAt <= :to) " +
            "GROUP BY s.keyword " +
            "ORDER BY COUNT(s) DESC")
    List<Object[]> aggregateKeywordCounts(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    void deleteAllByUser(User user);

}



