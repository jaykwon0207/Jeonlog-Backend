package com.jeonlog.exhibition_recommender.webhook.service;

import com.jeonlog.exhibition_recommender.webhook.domain.WebhookDelivery;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookDeliveryStatus;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookEventType;
import com.jeonlog.exhibition_recommender.webhook.exception.NonRetryableWebhookException;
import com.jeonlog.exhibition_recommender.webhook.exception.RetryableWebhookException;
import com.jeonlog.exhibition_recommender.webhook.repository.WebhookDeliveryRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private WebhookDeliveryRepository webhookDeliveryRepository;

    @Mock
    private DiscordWebhookClient discordWebhookClient;

    private WebhookDeliveryService webhookDeliveryService;

    @BeforeEach
    void setUp() {
        webhookDeliveryService = new WebhookDeliveryService(
                webhookDeliveryRepository,
                discordWebhookClient,
                new SimpleMeterRegistry(),
                new ResourcelessTransactionManager(),
                100,
                5,
                60,
                1800
        );
    }

    @Test
    void recordFailureStoresFailedForNonRetryableError() {
        webhookDeliveryService.recordFailure(
                WebhookEventType.REPORT_CREATED,
                10L,
                "https://discord.test",
                "message",
                new NonRetryableWebhookException("400")
        );

        ArgumentCaptor<WebhookDelivery> captor = ArgumentCaptor.forClass(WebhookDelivery.class);
        verify(webhookDeliveryRepository).save(captor.capture());

        WebhookDelivery saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(WebhookDeliveryStatus.FAILED);
        assertThat(saved.getAttemptCount()).isEqualTo(1);
        assertThat(saved.getNextRetryAt()).isNull();
        assertThat(saved.getPayload().length()).isLessThanOrEqualTo(4000);
    }

    @Test
    void recordFailureTruncatesOversizedPayload() {
        String longPayload = "x".repeat(5000);

        webhookDeliveryService.recordFailure(
                WebhookEventType.REPORT_CREATED,
                99L,
                "https://discord.test",
                longPayload,
                new RetryableWebhookException("temporary")
        );

        ArgumentCaptor<WebhookDelivery> captor = ArgumentCaptor.forClass(WebhookDelivery.class);
        verify(webhookDeliveryRepository).save(captor.capture());
        assertThat(captor.getValue().getPayload().length()).isEqualTo(4000);
    }

    @Test
    void replaySuccessMarksDeliveryAsSent() {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .eventType(WebhookEventType.REPORT_CREATED)
                .targetId(11L)
                .webhookUrl("https://discord.test")
                .payload("hello")
                .status(WebhookDeliveryStatus.PENDING_RETRY)
                .attemptCount(1)
                .nextRetryAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(webhookDeliveryRepository.findByStatusAndNextRetryAtLessThanEqualOrderByIdAsc(
                eq(WebhookDeliveryStatus.PENDING_RETRY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(delivery));
        when(webhookDeliveryRepository.findById(nullable(Long.class))).thenReturn(java.util.Optional.of(delivery));

        webhookDeliveryService.replayPendingDeliveries();

        assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.SENT);
    }

    @Test
    void replayRetryableFailureReschedulesDelivery() {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .eventType(WebhookEventType.USER_BLOCKED)
                .targetId(12L)
                .webhookUrl("https://discord.test")
                .payload("hello")
                .status(WebhookDeliveryStatus.PENDING_RETRY)
                .attemptCount(1)
                .nextRetryAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(webhookDeliveryRepository.findByStatusAndNextRetryAtLessThanEqualOrderByIdAsc(
                eq(WebhookDeliveryStatus.PENDING_RETRY),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of(delivery));
        when(webhookDeliveryRepository.findById(nullable(Long.class))).thenReturn(java.util.Optional.of(delivery));

        doThrow(new RetryableWebhookException("timeout"))
                .when(discordWebhookClient)
                .sendBlockWebhook(any(Long.class), any(String.class), any(String.class));

        webhookDeliveryService.replayPendingDeliveries();

        assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.PENDING_RETRY);
        assertThat(delivery.getAttemptCount()).isEqualTo(2);
        assertThat(delivery.getNextRetryAt()).isNotNull();
    }
}
