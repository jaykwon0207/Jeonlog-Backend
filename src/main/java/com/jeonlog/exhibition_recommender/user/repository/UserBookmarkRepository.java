package com.jeonlog.exhibition_recommender.user.repository;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.UserBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookmarkRepository extends JpaRepository<UserBookmark, Long> {
    boolean existsByUserAndExhibition(User user, Exhibition exhibition);

    Optional<UserBookmark> findByUserAndExhibition(User user, Exhibition exhibition);

    List<UserBookmark> findAllByUser(User user);

}
