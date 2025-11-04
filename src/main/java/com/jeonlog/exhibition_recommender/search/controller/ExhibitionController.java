package com.jeonlog.exhibition_recommender.search.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionImageResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.search.service.ExhibitionService;
import com.jeonlog.exhibition_recommender.search.service.SearchService;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;
    private final SearchService searchService;

    // 전체 전시 목록 조회
    @GetMapping
    public ApiResponse<List<ExhibitionImageResponseDto>> getAllExhibitions() {
        return ApiResponse.ok(exhibitionService.getAllExhibitions());
    }

    // 전체 전시 목록 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<List<ExhibitionResponseDto>> getExhibitions() {
        List<ExhibitionResponseDto> exhibitions = exhibitionService.getAllExhibitionsDetails();
        return ResponseEntity.ok(exhibitions);
    }


    // 특정 전시 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<ExhibitionDetailResponseDto> getExhibitionById(@PathVariable Long id) {
        return ApiResponse.ok(exhibitionService.getExhibitionDetailById(id));
    }

    // 전시 검색
    @GetMapping("/search")
    public ApiResponse<List<ExhibitionSearchResponseDto>> searchExhibitions(
            @RequestParam String query,
            @RequestParam(required = false) List<String> filter
    ) {
        // 검색 수행
        List<ExhibitionSearchResponseDto> results = exhibitionService.searchExhibitions(query, filter);
        return ApiResponse.ok(results);
    }

    // 검색 기록 (키워드만 기록, 인증된 사용자 기준)
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


    // 인기 검색어 랭킹 조회
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
}