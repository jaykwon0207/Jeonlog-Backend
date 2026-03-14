package com.jeonlog.exhibition_recommender.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportReason;
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
public class DiscordReportWebhookService {

    private final ObjectMapper objectMapper;

    @Value("${moderation.discord.report-webhook-url:${moderation.discord.webhook-url:}}")
    private String webhookUrl;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

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
                log.warn("[REPORT_WEBHOOK] failed reportId={}, status={}", report.getId(), response.statusCode());
            }
        } catch (Exception e) {
            log.warn("[REPORT_WEBHOOK] failed reportId={}, reason={}", report.getId(), e.getMessage());
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
