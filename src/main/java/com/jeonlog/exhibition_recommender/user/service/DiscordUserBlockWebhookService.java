package com.jeonlog.exhibition_recommender.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordUserBlockWebhookService {

    private final ObjectMapper objectMapper;

    @Value("${block.discord.webhook-url:${moderation.discord.block-webhook-url:${moderation.discord.webhook-url:}}}")
    private String webhookUrl;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public void sendUserBlocked(User blocker, User blocked) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String message = String.format(
                """
                ⛔ **사용자 차단 발생**
                • 차단자: `userId=%d`, nickname=`%s`
                • 차단대상: `userId=%d`, nickname=`%s`
                """,
                blocker.getId(),
                safe(blocker.getNickname()),
                blocked.getId(),
                safe(blocked.getNickname())
        );

        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                log.warn("[BLOCK_WEBHOOK] failed blockerId={}, blockedId={}, status={}",
                        blocker.getId(), blocked.getId(), response.statusCode());
            }
        } catch (Exception e) {
            log.warn("[BLOCK_WEBHOOK] failed blockerId={}, blockedId={}, reason={}",
                    blocker.getId(), blocked.getId(), e.getMessage());
        }
    }

    private String safe(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "-";
        }
        return nickname;
    }
}
