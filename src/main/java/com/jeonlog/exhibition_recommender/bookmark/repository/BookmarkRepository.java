// bookmark/repository/BookmarkRepository.java
package com.jeonlog.exhibition_recommender.bookmark.repository;

import com.jeonlog.exhibition_recommender.bookmark.domain.Bookmark;
import com.jeonlog.exhibition_recommender.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserIdAndExhibitionId(Long userId, Long exhibitionId);

    void deleteByUserIdAndExhibitionId(Long userId, Long exhibitionId);

    long countByExhibitionId(Long exhibitionId);

    Page<Bookmark> findByExhibitionId(Long exhibitionId, Pageable pageable);

    Page<Bookmark> findByUser(User user, Pageable pageable);

    Optional<Bookmark> findByUserIdAndExhibitionId(Long userId, Long exhibitionId);

}