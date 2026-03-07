package com.jeonlog.exhibition_recommender.report.dto;

import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportCreateResponse {
    private Long reportId;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime dueAt;
    private String message;

    public static ReportCreateResponse from(Report report) {
        return ReportCreateResponse.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .dueAt(report.getDueAt())
                .message("신고가 접수되었습니다. 24시간 내 검토 후 조치됩니다.")
                .build();
    }
}
