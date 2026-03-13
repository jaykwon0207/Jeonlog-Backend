package com.jeonlog.exhibition_recommender.notification.repository;

import com.jeonlog.exhibition_recommender.notification.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Slice<Notification> findByReceiverUserIdAndIdLessThanOrderByIdDesc(Long receiverUserId, Long lastId, Pageable pageable);

    Slice<Notification> findByReceiverUserIdOrderByIdDesc(Long receiverUserId, Pageable pageable);

    long countByReceiverUserIdAndIsReadFalse(Long receiverUserId);

    Optional<Notification> findByIdAndReceiverUserId(Long id, Long receiverUserId);

    boolean existsByDedupKey(String dedupKey);

    void deleteAllByReceiverUserIdOrActorUserId(Long receiverUserId, Long actorUserId);
}
