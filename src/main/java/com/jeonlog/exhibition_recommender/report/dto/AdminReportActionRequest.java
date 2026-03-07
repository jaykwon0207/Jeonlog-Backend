package com.jeonlog.exhibition_recommender.report.dto;

import com.jeonlog.exhibition_recommender.report.domain.ReportAction;
import com.jeonlog.exhibition_recommender.report.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportActionRequest {

    @NotNull
    private ReportStatus status;

    @NotNull
    private ReportAction action;

    private String adminMemo;
}
