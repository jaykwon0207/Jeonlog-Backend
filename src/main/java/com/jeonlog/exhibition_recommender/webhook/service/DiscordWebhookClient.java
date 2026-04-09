package com.jeonlog.exhibition_recommender.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.webhook.exception.NonRetryableWebhookException;
import com.jeonlog.exhibition_recommender.webhook.exception.RetryableWebhookException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class DiscordWebhookClient {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public DiscordWebhookClient(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public void sendReportWebhook(Long reportId, String webhookUrl, String message) {
        send("report", reportId, webhookUrl, message);
    }

    public void sendBlockWebhook(Long blockEventId, String webhookUrl, String message) {
        send("block", blockEventId, webhookUrl, message);
    }

    private void send(String webhookType, Long targetId, String webhookUrl, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new NonRetryableWebhookException("webhook url is blank");
        }

        Counter.builder("webhook_send_attempt_total")
                .tag("webhookType", webhookType)
                .register(meterRegistry)
                .increment();

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("content", message);

            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();

            if (statusCode >= 500 || statusCode == 429) {
                throw new RetryableWebhookException("transient webhook error status=" + statusCode);
            }
            if (statusCode >= 400) {
                throw new NonRetryableWebhookException("non-retryable webhook error status=" + statusCode);
            }
            if (statusCode >= 300) {
                throw new RetryableWebhookException("unexpected redirect webhook status=" + statusCode);
            }

            Counter.builder("webhook_send_success_total")
                    .tag("webhookType", webhookType)
                    .register(meterRegistry)
                    .increment();
        } catch (IOException e) {
            throw new RetryableWebhookException("io failure while sending webhook", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryableWebhookException("interrupted while sending webhook", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RetryableWebhookException("unexpected webhook failure", e);
        } finally {
            sample.stop(Timer.builder("webhook_send_duration")
                    .tag("webhookType", webhookType)
                    .register(meterRegistry));
        }
    }
}
