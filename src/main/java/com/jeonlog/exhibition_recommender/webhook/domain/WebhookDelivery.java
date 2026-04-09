package com.jeonlog.exhibition_recommender.webhook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "webhook_delivery",
        indexes = {
                @Index(name = "idx_webhook_delivery_status_next_retry", columnList = "status,next_retry_at"),
                @Index(name = "idx_webhook_delivery_event_created", columnList = "event_type,created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private WebhookEventType eventType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "webhook_url", nullable = false, length = 1000)
    private String webhookUrl;

    @Column(name = "payload", nullable = false, length = 4000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WebhookDeliveryStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public WebhookDelivery(
            WebhookEventType eventType,
            Long targetId,
            String webhookUrl,
            String payload,
            WebhookDeliveryStatus status,
            int attemptCount,
            String lastError,
            LocalDateTime nextRetryAt
    ) {
        this.eventType = eventType;
        this.targetId = targetId;
        this.webhookUrl = webhookUrl;
        this.payload = payload;
        this.status = status;
        this.attemptCount = attemptCount;
        this.lastError = lastError;
        this.nextRetryAt = nextRetryAt;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markSent() {
        this.status = WebhookDeliveryStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.nextRetryAt = null;
    }

    public void markPendingRetry(int nextAttemptCount, LocalDateTime nextRetryAt, String reason) {
        this.status = WebhookDeliveryStatus.PENDING_RETRY;
        this.attemptCount = nextAttemptCount;
        this.nextRetryAt = nextRetryAt;
        this.lastError = truncateReason(reason);
    }

    public void markFailed(int finalAttemptCount, String reason) {
        this.status = WebhookDeliveryStatus.FAILED;
        this.attemptCount = finalAttemptCount;
        this.nextRetryAt = null;
        this.lastError = truncateReason(reason);
    }

    private String truncateReason(String reason) {
        if (reason == null) {
            return null;
        }
        if (reason.length() <= 1000) {
            return reason;
        }
        return reason.substring(0, 1000);
    }
}
