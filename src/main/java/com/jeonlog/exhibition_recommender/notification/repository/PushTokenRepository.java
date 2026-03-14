package com.jeonlog.exhibition_recommender.notification.repository;

import com.jeonlog.exhibition_recommender.notification.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByUserId(Long userId);

    List<PushToken> findAllByIsActiveTrue();

    void deleteByUserId(Long userId);
}
