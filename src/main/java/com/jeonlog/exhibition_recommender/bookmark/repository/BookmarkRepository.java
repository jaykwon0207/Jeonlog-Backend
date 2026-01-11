package com.jeonlog.exhibition_recommender.bookmark.repository;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndExhibition(User user, Exhibition exhibition);

    Optional<Bookmark> findByUserAndExhibition(User user, Exhibition exhibition);

    void deleteByUserAndExhibition(User user, Exhibition exhibition);

    long countByExhibition(Exhibition exhibition);

    Page<Bookmark> findByExhibition(Exhibition exhibition, Pageable pageable);

    Page<Bookmark> findByUser(User user, Pageable pageable);
}