package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationImageDto;
import com.jeonlog.exhibition_recommender.recommendation.service.RecommendationService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public ApiResponse<List<RecommendationImageDto>> getRecommendations(
            @CurrentUser User user
    ) {
        List<Exhibition> recs = recommendationService.recommend(user.getId());

        List<RecommendationImageDto> body = recs.stream()
                .map(RecommendationImageDto::from)
                .toList();

        return ApiResponse.ok(body);
    }
}
