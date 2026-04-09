package com.jeonlog.exhibition_recommender.webhook.scheduler;

import com.jeonlog.exhibition_recommender.webhook.service.WebhookDeliveryService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class WebhookReplayScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookReplayScheduler.class);

    private final WebhookDeliveryService webhookDeliveryService;

    public WebhookReplayScheduler(WebhookDeliveryService webhookDeliveryService) {
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Scheduled(cron = "${webhook.replay.cron:0 */2 * * * *}", zone = "Asia/Seoul")
    @SchedulerLock(name = "webhookReplayScheduler_replay", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void replay() {
        Instant startedAt = Instant.now();
        log.info("[SCHEDULER] webhook_replay_started");
        int processed = webhookDeliveryService.replayPendingDeliveries();
        long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
        log.info("[SCHEDULER] webhook_replay_completed processedCount={} durationMs={}", processed, durationMs);
    }
}
