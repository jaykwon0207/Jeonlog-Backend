package com.jeonlog.exhibition_recommender.bookmark.repository;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserBookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndExhibition(User user, Exhibition exhibition);

    Optional<Bookmark> findByUserAndExhibition(User user, Exhibition exhibition);

    List<Bookmark> findAllByUser(User user);

    @Modifying
    @Query("DELETE FROM Bookmark ub " +
            "WHERE ub.exhibition.endDate < :today")
    void deleteBookmarksForEndedExhibitions(@Param("today") LocalDate today);

}
