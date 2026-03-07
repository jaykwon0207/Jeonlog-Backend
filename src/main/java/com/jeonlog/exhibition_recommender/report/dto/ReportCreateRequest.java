package com.jeonlog.exhibition_recommender.report.dto;

import com.jeonlog.exhibition_recommender.report.domain.ReportReason;
import com.jeonlog.exhibition_recommender.report.domain.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportCreateRequest {

    @NotNull
    private ReportTargetType targetType;

    @NotNull
    private Long targetId;

    @NotNull
    private ReportReason reason;

    private String detail;
}
