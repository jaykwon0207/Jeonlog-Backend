package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.service.PopularByAgeRecommendationService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations/age")
public class PopularByAgeRecommendationController {

    private final PopularByAgeRecommendationService popularByAgeRecommendationService;

    //ageGroup 1=유아청소년, 2=20-30대, 3=40-50대, 4=60대 이상
    @GetMapping("/{ageGroup}")
    public ApiResponse<List<RecommendationDto>> getPopularByAge(
            @PathVariable int ageGroup
    ) {
        // 최근 30일간의 클릭/북마크 데이터 기준으로 계산
        int days = 30;
        double clickWeight = 1.0;
        double bookmarkWeight = 1.0;

        List<RecommendationDto> recommendations =
                popularByAgeRecommendationService.getPopularByAgeGroup(ageGroup, days, clickWeight, bookmarkWeight);

        return ApiResponse.ok(recommendations);
    }
}
