package com.jeonlog.exhibition_recommender.webhook.service;

import com.jeonlog.exhibition_recommender.webhook.domain.WebhookDelivery;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookDeliveryStatus;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookEventType;
import com.jeonlog.exhibition_recommender.webhook.repository.WebhookDeliveryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WebhookDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryService.class);

    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final DiscordWebhookClient discordWebhookClient;
    private final MeterRegistry meterRegistry;
    private final TransactionTemplate transactionTemplate;
    private final int batchSize;
    private final int maxAttempts;
    private final long initialBackoffSeconds;
    private final long maxBackoffSeconds;

    public WebhookDeliveryService(
            WebhookDeliveryRepository webhookDeliveryRepository,
            DiscordWebhookClient discordWebhookClient,
            MeterRegistry meterRegistry,
            org.springframework.transaction.PlatformTransactionManager transactionManager,
            @Value("${webhook.replay.batch-size:100}") int batchSize,
            @Value("${webhook.replay.max-attempts:5}") int maxAttempts,
            @Value("${webhook.replay.initial-backoff-seconds:60}") long initialBackoffSeconds,
            @Value("${webhook.replay.max-backoff-seconds:1800}") long maxBackoffSeconds
    ) {
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.discordWebhookClient = discordWebhookClient;
        this.meterRegistry = meterRegistry;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.initialBackoffSeconds = initialBackoffSeconds;
        this.maxBackoffSeconds = maxBackoffSeconds;
    }

    @Transactional
    public void recordFailure(WebhookEventType eventType, Long targetId, String webhookUrl, String payload, Throwable throwable) {
        boolean retryable = WebhookFailureClassifier.isRetryable(throwable);
        String reason = WebhookFailureClassifier.reason(throwable);

        LocalDateTime now = LocalDateTime.now();
        WebhookDelivery delivery = WebhookDelivery.builder()
                .eventType(eventType)
                .targetId(targetId)
                .webhookUrl(truncate(webhookUrl, 1000))
                .payload(truncate(payload, 4000))
                .status(retryable ? WebhookDeliveryStatus.PENDING_RETRY : WebhookDeliveryStatus.FAILED)
                .attemptCount(1)
                .lastError(truncate(reason, 1000))
                .nextRetryAt(retryable ? now.plusSeconds(initialBackoffSeconds) : null)
                .build();

        webhookDeliveryRepository.save(delivery);

        Counter.builder("webhook_send_failure_total")
                .tag("webhookType", eventType.name())
                .tag("retryable", String.valueOf(retryable))
                .register(meterRegistry)
                .increment();

        log.warn(
                "[WEBHOOK_DELIVERY] recorded failure eventType={}, targetId={}, retryable={}, reason={}",
                eventType,
                targetId,
                retryable,
                reason
        );
    }

    public int replayPendingDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<WebhookDelivery> pending = webhookDeliveryRepository.findByStatusAndNextRetryAtLessThanEqualOrderByIdAsc(
                WebhookDeliveryStatus.PENDING_RETRY,
                now,
                PageRequest.of(0, batchSize)
        );
        log.info("[WEBHOOK] replay_start pendingCount={} batchSize={}", pending.size(), batchSize);

        for (WebhookDelivery delivery : pending) {
            replaySingle(delivery.getId(), now);
        }
        log.info("[WEBHOOK] replay_complete processedCount={}", pending.size());
        return pending.size();
    }

    private void replaySingle(Long deliveryId, LocalDateTime now) {
        WebhookDelivery delivery = webhookDeliveryRepository.findById(deliveryId)
                .orElse(null);
        if (delivery == null || delivery.getStatus() != WebhookDeliveryStatus.PENDING_RETRY) {
            return;
        }

        try {
            dispatch(delivery);
            markSent(deliveryId);
            log.info(
                    "[WEBHOOK] replay_sent deliveryId={} eventType={} targetId={}",
                    deliveryId,
                    delivery.getEventType(),
                    delivery.getTargetId()
            );
        } catch (Exception ex) {
            boolean retryable = WebhookFailureClassifier.isRetryable(ex);
            String reason = truncate(WebhookFailureClassifier.reason(ex), 1000);
            updateReplayFailure(deliveryId, retryable, reason, now);
            log.warn(
                    "[WEBHOOK] replay_failed deliveryId={} eventType={} targetId={} retryable={} reason={}",
                    deliveryId,
                    delivery.getEventType(),
                    delivery.getTargetId(),
                    retryable,
                    reason
            );
        }
    }

    private void markSent(Long deliveryId) {
        transactionTemplate.executeWithoutResult(status -> webhookDeliveryRepository.findById(deliveryId)
                .filter(delivery -> delivery.getStatus() == WebhookDeliveryStatus.PENDING_RETRY)
                .ifPresent(WebhookDelivery::markSent));
    }

    private void updateReplayFailure(Long deliveryId, boolean retryable, String reason, LocalDateTime now) {
        transactionTemplate.executeWithoutResult(status -> webhookDeliveryRepository.findById(deliveryId)
                .filter(delivery -> delivery.getStatus() == WebhookDeliveryStatus.PENDING_RETRY)
                .ifPresent(delivery -> {
                    int nextAttemptCount = delivery.getAttemptCount() + 1;
                    if (!retryable || nextAttemptCount >= maxAttempts) {
                        delivery.markFailed(nextAttemptCount, reason);
                        log.error(
                                "[WEBHOOK] replay_final_failed deliveryId={} eventType={} targetId={} attemptCount={} reason={}",
                                deliveryId,
                                delivery.getEventType(),
                                delivery.getTargetId(),
                                nextAttemptCount,
                                reason
                        );
                        return;
                    }

                    long backoffSeconds = calculateBackoffSeconds(nextAttemptCount - 1);
                    delivery.markPendingRetry(nextAttemptCount, now.plusSeconds(backoffSeconds), reason);
                    log.warn(
                            "[WEBHOOK] replay_retry_scheduled deliveryId={} eventType={} targetId={} attemptCount={} backoffSeconds={} reason={}",
                            deliveryId,
                            delivery.getEventType(),
                            delivery.getTargetId(),
                            nextAttemptCount,
                            backoffSeconds,
                            reason
                    );
                }));
    }

    private void dispatch(WebhookDelivery delivery) {
        if (delivery.getEventType() == WebhookEventType.REPORT_CREATED) {
            discordWebhookClient.sendReportWebhook(delivery.getTargetId(), delivery.getWebhookUrl(), delivery.getPayload());
            return;
        }
        if (delivery.getEventType() == WebhookEventType.USER_BLOCKED) {
            discordWebhookClient.sendBlockWebhook(delivery.getTargetId(), delivery.getWebhookUrl(), delivery.getPayload());
            return;
        }
        throw new IllegalArgumentException("Unsupported webhook event type: " + delivery.getEventType());
    }

    private long calculateBackoffSeconds(int retryCount) {
        long candidate = (long) (initialBackoffSeconds * Math.pow(2, Math.max(0, retryCount - 1)));
        return Math.min(candidate, maxBackoffSeconds);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
