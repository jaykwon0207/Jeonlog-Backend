package com.jeonlog.exhibition_recommender.exhibition.controller;


import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.service.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    //전체 전시 목록 조회
    @GetMapping
    public List<ExhibitionResponseDto> getAllExhibitions() {
        return exhibitionService.getAllExhibitions();
    }

    //특정 전시 상세 조회
    @GetMapping("/{id}")
    public ExhibitionDetailResponseDto getExhibitionById(@PathVariable Long id) {
        return exhibitionService.getExhibitionDetailById(id);
    }

    // 전시 검색 (제목, 작가, 장르, 장소등 필터링)
    @GetMapping("/search")
    public List<ExhibitionSearchResponseDto> searchExhibitions(
            @RequestParam String query,
            @RequestParam(required = false) List<String> filter

    ) {
        return exhibitionService.searchExhibitions(query, filter);
    }
}