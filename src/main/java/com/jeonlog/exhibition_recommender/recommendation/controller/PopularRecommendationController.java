package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.recommendation.dto.PopularRecommendationDto;
import com.jeonlog.exhibition_recommender.recommendation.service.PopularRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class PopularRecommendationController {

    private final PopularRecommendationService service;

    @GetMapping("/popular")
    public ResponseEntity<List<PopularRecommendationDto>> getPopular() {
        return ResponseEntity.ok(service.getPopular());
    }
}
