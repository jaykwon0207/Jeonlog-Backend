package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.service.PopularByAgeRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PopularByAgeRecommendationController {

    private final PopularByAgeRecommendationService service;


    //id = 10,20,30,40,50,60 (60은 60대 이상)
    @GetMapping("/recommendations/age/{id}")
    public ApiResponse<List<RecommendationDto>> getPopularByAgeDecade(
            @PathVariable("id") int decade,
            @RequestParam(name = "days", defaultValue = "30") int days,
            @RequestParam(name = "clickWeight", defaultValue = "1.0") double clickWeight,
            @RequestParam(name = "bookmarkWeight", defaultValue = "1.0") double bookmarkWeight
    ) {
        return ApiResponse.ok(service.getPopularByAgeDecade(decade, days, clickWeight, bookmarkWeight));
    }
}
