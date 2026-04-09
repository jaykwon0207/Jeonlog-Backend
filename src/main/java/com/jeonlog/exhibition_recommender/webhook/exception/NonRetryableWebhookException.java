package com.jeonlog.exhibition_recommender.webhook.exception;

public class NonRetryableWebhookException extends RuntimeException {

    public NonRetryableWebhookException(String message) {
        super(message);
    }

    public NonRetryableWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
