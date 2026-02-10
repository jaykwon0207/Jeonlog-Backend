package com.jeonlog.exhibition_recommender.bookmark.repository;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    void deleteByUserId(Long userId);


    Optional<Bookmark> findByUserAndExhibition(User user, Exhibition exhibition);


    long countByExhibition(Exhibition exhibition);


    Page<Bookmark> findByUser(User user, Pageable pageable);

    @Query("select b.user.id from Bookmark b where b.exhibition.id = :exhibitionId and b.notifyEnabled = true")
    List<Long> findNotifiedUserIdsByExhibitionId(@Param("exhibitionId") Long exhibitionId);

    void deleteAllByUser(User user);

}