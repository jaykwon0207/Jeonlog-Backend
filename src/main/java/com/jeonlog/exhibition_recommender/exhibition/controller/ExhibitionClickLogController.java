package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.exhibition.dto.CategoryCountDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ClickLogCreateResponse;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionClickLogDto;
import com.jeonlog.exhibition_recommender.exhibition.service.ExhibitionClickLogService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions")
public class ExhibitionClickLogController {

    private final ExhibitionClickLogService clickLogService;
    private final UserRepository userRepository;

    @PostMapping("/{id}/click")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClickLogCreateResponse>> saveClickLog(
            @PathVariable("id") Long exhibitionId,
            @RequestBody(required = false) ExhibitionClickLogDto requestDto
    ) {
        User user = getCurrentUserOrThrow();
        ExhibitionClickLogService.SavedClick saved = clickLogService.saveClick(exhibitionId, user, requestDto);

        ClickLogCreateResponse body = ClickLogCreateResponse.builder()
                .logId(saved.getLogId())
                .exhibitionId(exhibitionId)
                .userId(user.getId())
                .clickedAt(saved.getClickedAt().toString())
                .build();

        return ResponseEntity
                .created(URI.create("/api/exhibitions/" + exhibitionId + "/click-logs/" + body.getLogId()))
                .body(ApiResponse.ok(body));
    }

    @GetMapping("/{id}/click-stats/age-group")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryCountDto>>> getClickStatsByAgeGroupForExhibition(
            @PathVariable("id") Long exhibitionId) {
        List<CategoryCountDto> stats = clickLogService.getClickStatsByAgeGroupForExhibition(exhibitionId);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/{id}/click-stats/gender")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryCountDto>>> getClickStatsByGenderForExhibition(
            @PathVariable("id") Long exhibitionId) {
        List<CategoryCountDto> stats = clickLogService.getClickStatsByGenderForExhibition(exhibitionId);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
    }
}