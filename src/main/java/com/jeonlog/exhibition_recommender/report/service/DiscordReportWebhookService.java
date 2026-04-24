package com.jeonlog.exhibition_recommender.report.service;

import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportReason;
import com.jeonlog.exhibition_recommender.webhook.domain.WebhookEventType;
import com.jeonlog.exhibition_recommender.webhook.service.DiscordWebhookClient;
import com.jeonlog.exhibition_recommender.webhook.service.WebhookDeliveryService;
import com.jeonlog.exhibition_recommender.webhook.service.WebhookFailureClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DiscordReportWebhookService {

    private static final Logger log = LoggerFactory.getLogger(DiscordReportWebhookService.class);

    private final DiscordWebhookClient discordWebhookClient;
    private final WebhookDeliveryService webhookDeliveryService;

    public DiscordReportWebhookService(
            DiscordWebhookClient discordWebhookClient,
            WebhookDeliveryService webhookDeliveryService
    ) {
        this.discordWebhookClient = discordWebhookClient;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Value("${moderation.discord.report-webhook-url:${moderation.discord.webhook-url:}}")
    private String webhookUrl;

    public void sendNewReport(Report report) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String detail = (report.getDetail() == null || report.getDetail().isBlank())
                ? "-"
                : report.getDetail();
        String message = String.format(
                """
                🚨 **신규 신고 접수**
                • 신고 ID: `%d`
                • 대상: `%s:%d`
                • 사유: `%s` (%s)
                • 신고자: `userId=%d`
                • 상세: %s
                • 접수시각: `%s`
                """,
                report.getId(),
                report.getTargetType().name(),
                report.getTargetId(),
                report.getReason().name(),
                reasonLabel(report.getReason()),
                report.getReporter().getId(),
                detail,
                report.getCreatedAt()
        );

        try {
            discordWebhookClient.sendReportWebhook(report.getId(), webhookUrl, message);
        } catch (Exception e) {
            webhookDeliveryService.recordFailure(
                    WebhookEventType.REPORT_CREATED,
                    report.getId(),
                    webhookUrl,
                    message,
                    e
            );
            log.warn(
                    "[REPORT_WEBHOOK] failed reportId={}, retryable={}, reason={}",
                    report.getId(),
                    WebhookFailureClassifier.isRetryable(e),
                    WebhookFailureClassifier.reason(e)
            );
        }
    }

    private String reasonLabel(ReportReason reason) {
        return switch (reason) {
            case SPAM_AD_PROMOTION -> "스팸/광고/홍보";
            case ABUSE_HATE_HARASSMENT -> "욕설/혐오/괴롭힘";
            case SEXUAL_CONTENT -> "음란/선정적 콘텐츠";
            case PERSONAL_INFO_EXPOSURE -> "개인정보 노출";
            case COPYRIGHT_RIGHTS_INFRINGEMENT -> "저작권/권리 침해";
            case OTHER -> "기타";
        };
    }
}
