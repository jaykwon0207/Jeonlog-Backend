package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.service.PopularByGenderRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PopularByGenderRecommendationController {

    private final PopularByGenderRecommendationService service;

    //id: 0=남자, 1=여자
    @GetMapping("/recommendations/gender/{id}")
    public ApiResponse<List<RecommendationDto>> getPopularByGender(
            @PathVariable("id") int genderId,
            @RequestParam(name = "days", defaultValue = "30") int days,
            @RequestParam(name = "clickWeight", defaultValue = "1.0") double clickWeight,
            @RequestParam(name = "bookmarkWeight", defaultValue = "1.0") double bookmarkWeight
    ) {
        return ApiResponse.ok(service.getPopularByGender(genderId, days, clickWeight, bookmarkWeight));
    }
}
