package com.jeonlog.exhibition_recommender.user.service;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookEventType;
import com.jeonlog.exhibition_recommender.webhook.service.DiscordWebhookClient;
import com.jeonlog.exhibition_recommender.webhook.service.WebhookDeliveryService;
import com.jeonlog.exhibition_recommender.webhook.service.WebhookFailureClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiscordUserBlockWebhookService {

    private static final Logger log = LoggerFactory.getLogger(DiscordUserBlockWebhookService.class);

    private final DiscordWebhookClient discordWebhookClient;
    private final WebhookDeliveryService webhookDeliveryService;

    public DiscordUserBlockWebhookService(
            DiscordWebhookClient discordWebhookClient,
            WebhookDeliveryService webhookDeliveryService
    ) {
        this.discordWebhookClient = discordWebhookClient;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Value("${block.discord.webhook-url:${moderation.discord.block-webhook-url:${moderation.discord.webhook-url:}}}")
    private String webhookUrl;

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

        try {
            discordWebhookClient.sendBlockWebhook(blocked.getId(), webhookUrl, message);
        } catch (Exception e) {
            webhookDeliveryService.recordFailure(
                    WebhookEventType.USER_BLOCKED,
                    blocked.getId(),
                    webhookUrl,
                    message,
                    e
            );
            log.warn(
                    "[BLOCK_WEBHOOK] failed blockerId={}, blockedId={}, retryable={}, reason={}",
                    blocker.getId(),
                    blocked.getId(),
                    WebhookFailureClassifier.isRetryable(e),
                    WebhookFailureClassifier.reason(e)
            );
        }
    }

    private String safe(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "-";
        }
        return nickname;
    }
}
