package com.jeonlog.exhibition_recommender.bookmark.repository;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExhibitionBookmarkRepository extends JpaRepository<Bookmark, Long> {
    Long countByExhibition(Exhibition exhibition);


}
