package com.jeonlog.exhibition_recommender.search.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.search.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    // 전체 전시 목록 조회
    @GetMapping
    public ApiResponse<List<ExhibitionResponseDto>> getAllExhibitions() {
        return ApiResponse.ok(exhibitionService.getAllExhibitions());
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
        return ApiResponse.ok(exhibitionService.searchExhibitions(query, filter));
    }
}