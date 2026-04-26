package com.jeonlog.exhibition_recommender.search.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
import com.jeonlog.exhibition_recommender.search.service.ExhibitionService;
import com.jeonlog.exhibition_recommender.search.service.SearchService;
import com.jeonlog.exhibition_recommender.user.domain.Role;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;
    private final SearchService searchService;

    // ✅ 1. 전체 전시 목록 조회
    @GetMapping
    public ApiResponse<List<ExhibitionResponseDto>> getAllExhibitions() {
        return ApiResponse.ok(exhibitionService.getAllExhibitions());
    }

    // ✅ 2. 전체 전시 목록 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<List<ExhibitionResponseDto>> getExhibitions() {
        List<ExhibitionResponseDto> exhibitions = exhibitionService.getAllExhibitionsDetails();
        return ResponseEntity.ok(exhibitions);
    }

    // ✅ 3. 특정 전시 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<ExhibitionDetailResponseDto> getExhibitionById(@PathVariable Long id) {
        return ApiResponse.ok(exhibitionService.getExhibitionDetailById(id));
    }

    // ✅ 4. 전시 검색
    @GetMapping("/search")
    public ApiResponse<List<ExhibitionSearchResponseDto>> searchExhibitions(
            @RequestParam String query,
            @RequestParam(required = false) List<String> filter
    ) {
        List<ExhibitionSearchResponseDto> results = exhibitionService.searchExhibitions(query, filter);
        return ApiResponse.ok(results);
    }

    // ✅ 5. 검색 로그 저장 (로그인 사용자)
    @PostMapping("/search/log")
    public ApiResponse<String> logSearch(
            @CurrentUser User user,
            @RequestParam String query
    ) {
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다.");
        }
        searchService.recordSearch(user.getEmail(), query);
        return ApiResponse.ok("검색 로그가 저장되었습니다.");
    }

    // ✅ 6. 인기 검색어 랭킹 조회
    @GetMapping("/search/rank")
    public ApiResponse<List<KeywordRankDto>> getSearchRank(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        java.time.LocalDateTime fromDt = from == null || from.isBlank() ? null : java.time.LocalDateTime.parse(from);
        java.time.LocalDateTime toDt = to == null || to.isBlank() ? null : java.time.LocalDateTime.parse(to);
        return ApiResponse.ok(searchService.getTopKeywords(fromDt, toDt, limit));
    }

    // ✅ 7. 포스터 URL 업데이트 (S3 업로드 후 DB 반영)
    @PostMapping("/{id}/poster-url")
    public ApiResponse<Void> updatePosterUrl(
            @PathVariable Long id,
            @RequestParam String posterUrl,
            @CurrentUser User user
    ) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
        exhibitionService.updatePosterUrl(id, posterUrl, user);

        log.info("✅ [포스터 업데이트] userId={} exhibitionId={} url={}",
                user.getId(), id, posterUrl);

        return ApiResponse.ok(null);
    }
}
