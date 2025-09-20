package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.service.PopularRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PopularRecommendationController {

    private final PopularRecommendationService popularService;


    //최근 30일, 클릭 1.0 / 북마크 1.0
    @GetMapping("/recommendations/popular")
    public ApiResponse<List<RecommendationDto>> getPopular(
            @RequestParam(name = "days", defaultValue = "30") int days,
            @RequestParam(name = "clickWeight", defaultValue = "1.0") double clickWeight,
            @RequestParam(name = "bookmarkWeight", defaultValue = "1.0") double bookmarkWeight
    ) {
        return ApiResponse.ok(popularService.getPopular(days, clickWeight, bookmarkWeight));
    }
}
