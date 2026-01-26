package com.jeonlog.exhibition_recommender.notification.repository;

import com.jeonlog.exhibition_recommender.notification.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    Optional<PushToken> findByUserIdAndIsActiveTrue(Long userId);
    Optional<PushToken> findByUserId(Long userId);

}