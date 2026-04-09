package com.jeonlog.exhibition_recommender.webhook.repository;

import com.jeonlog.exhibition_recommender.webhook.domain.WebhookDelivery;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookDeliveryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findByStatusAndNextRetryAtLessThanEqualOrderByIdAsc(
            WebhookDeliveryStatus status,
            LocalDateTime nextRetryAt,
            Pageable pageable
    );
}
