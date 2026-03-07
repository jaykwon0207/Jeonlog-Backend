package com.jeonlog.exhibition_recommender.report.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.report.domain.ReportStatus;
import com.jeonlog.exhibition_recommender.report.dto.AdminReportActionRequest;
import com.jeonlog.exhibition_recommender.report.dto.ReportAdminItemResponse;
import com.jeonlog.exhibition_recommender.report.service.ReportService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class ReportAdminController {

    private final ReportService reportService;

    @GetMapping
    public ApiResponse<List<ReportAdminItemResponse>> getReports(
            @CurrentUser User user,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "false") boolean overdueOnly
    ) {
        return ApiResponse.ok(reportService.getAdminReports(user, status, overdueOnly));
    }

    @PatchMapping("/{reportId}")
    public ApiResponse<ReportAdminItemResponse> act(
            @CurrentUser User user,
            @PathVariable Long reportId,
            @RequestBody @Valid AdminReportActionRequest request
    ) {
        return ApiResponse.ok(reportService.act(user, reportId, request));
    }
}
