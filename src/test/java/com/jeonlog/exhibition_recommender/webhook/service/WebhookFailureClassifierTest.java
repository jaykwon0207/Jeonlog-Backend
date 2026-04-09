package com.jeonlog.exhibition_recommender.webhook.service;

import com.jeonlog.exhibition_recommender.webhook.exception.NonRetryableWebhookException;
import com.jeonlog.exhibition_recommender.webhook.exception.RetryableWebhookException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookFailureClassifierTest {

    @Test
    void nonRetryableExceptionIsClassifiedAsFalse() {
        boolean retryable = WebhookFailureClassifier.isRetryable(new NonRetryableWebhookException("bad request"));
        assertThat(retryable).isFalse();
    }

    @Test
    void retryableExceptionIsClassifiedAsTrue() {
        boolean retryable = WebhookFailureClassifier.isRetryable(new RetryableWebhookException("timeout"));
        assertThat(retryable).isTrue();
    }

    @Test
    void unknownExceptionDefaultsToNonRetryable() {
        boolean retryable = WebhookFailureClassifier.isRetryable(new RuntimeException("unexpected"));
        assertThat(retryable).isFalse();
    }
}
