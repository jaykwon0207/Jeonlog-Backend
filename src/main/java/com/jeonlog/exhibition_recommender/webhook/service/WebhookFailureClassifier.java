package com.jeonlog.exhibition_recommender.webhook.service;

import com.jeonlog.exhibition_recommender.webhook.exception.NonRetryableWebhookException;
import com.jeonlog.exhibition_recommender.webhook.exception.RetryableWebhookException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.io.IOException;
import java.net.http.HttpTimeoutException;

public final class WebhookFailureClassifier {

    private WebhookFailureClassifier() {
    }

    public static boolean isRetryable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof NonRetryableWebhookException) {
                return false;
            }
            if (current instanceof IllegalArgumentException) {
                return false;
            }
            if (current instanceof RetryableWebhookException
                    || current instanceof IOException
                    || current instanceof HttpTimeoutException
                    || current instanceof CallNotPermittedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static String reason(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + ": " + message;
    }
}
