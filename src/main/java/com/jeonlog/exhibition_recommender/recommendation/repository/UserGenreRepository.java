package com.jeonlog.exhibition_recommender.recommendation.repository;

import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    Optional<UserGenre> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
