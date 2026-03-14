package com.jeonlog.exhibition_recommender.report.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.report.dto.MyReportNoticeResponse;
import com.jeonlog.exhibition_recommender.report.dto.ReportCreateRequest;
import com.jeonlog.exhibition_recommender.report.dto.ReportCreateResponse;
import com.jeonlog.exhibition_recommender.report.service.ReportService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ApiResponse<ReportCreateResponse> create(
            @CurrentUser User user,
            @RequestBody @Valid ReportCreateRequest request
    ) {
        return ApiResponse.ok(reportService.create(user, request));
    }

    @GetMapping("/me/notices")
    public ApiResponse<List<MyReportNoticeResponse>> getMyNotices(@CurrentUser User user) {
        return ApiResponse.ok(reportService.getMyPendingNotices(user));
    }

    @PatchMapping("/me/notices/{reportId}/read")
    public ApiResponse<String> markMyNoticeRead(
            @CurrentUser User user,
            @PathVariable Long reportId
    ) {
        reportService.markNoticeRead(user, reportId);
        return ApiResponse.ok("제재 알림을 확인 처리했습니다.");
    }
}
