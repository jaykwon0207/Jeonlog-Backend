package com.jeonlog.exhibition_recommender.webhook.exception;

public class RetryableWebhookException extends RuntimeException {

    public RetryableWebhookException(String message) {
        super(message);
    }

    public RetryableWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
