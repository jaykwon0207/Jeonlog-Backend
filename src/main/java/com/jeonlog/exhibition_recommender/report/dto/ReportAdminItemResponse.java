package com.jeonlog.exhibition_recommender.report.dto;

import com.jeonlog.exhibition_recommender.report.domain.Report;
import com.jeonlog.exhibition_recommender.report.domain.ReportAction;
import com.jeonlog.exhibition_recommender.report.domain.ReportReason;
import com.jeonlog.exhibition_recommender.report.domain.ReportStatus;
import com.jeonlog.exhibition_recommender.report.domain.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportAdminItemResponse {
    private Long reportId;
    private Long reporterUserId;
    private String reporterNickname;
    private Long reportedUserId;
    private String reportedUserNickname;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private String detail;
    private ReportStatus status;
    private ReportAction action;
    private String adminMemo;
    private LocalDateTime createdAt;
    private LocalDateTime dueAt;
    private LocalDateTime processedAt;

    public static ReportAdminItemResponse from(Report report) {
        return ReportAdminItemResponse.builder()
                .reportId(report.getId())
                .reporterUserId(report.getReporter().getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedUserId(report.getReportedUser().getId())
                .reportedUserNickname(report.getReportedUser().getNickname())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .action(report.getAction())
                .adminMemo(report.getAdminMemo())
                .createdAt(report.getCreatedAt())
                .dueAt(report.getDueAt())
                .processedAt(report.getProcessedAt())
                .build();
    }
}
