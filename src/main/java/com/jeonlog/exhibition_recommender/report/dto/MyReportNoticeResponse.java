package com.jeonlog.exhibition_recommender.report.dto;

import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportAction;
import com.jeonlog.exhibition_recommender.report.domain.ReportReason;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyReportNoticeResponse {
    private Long reportId;
    private ReportReason reason;
    private ReportAction action;
    private LocalDateTime processedAt;

    public static MyReportNoticeResponse from(Report report) {
        return MyReportNoticeResponse.builder()
                .reportId(report.getId())
                .reason(report.getReason())
                .action(report.getAction())
                .processedAt(report.getProcessedAt())
                .build();
    }
}
