package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.recommendation.dto.InitialChoiceRequest;
import com.jeonlog.exhibition_recommender.recommendation.dto.InitialExhibitionDto;
import com.jeonlog.exhibition_recommender.recommendation.service.InitialRecommendationService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/initial")
public class InitialRecommendationController {

    private final InitialRecommendationService initialService;

    @GetMapping("/recommendations")
    public ApiResponse<List<InitialExhibitionDto>> listInitial() {
        return ApiResponse.ok(initialService.listInitialExhibitions());
    }

    //사용자가 선택 완료 시 호출
    @PostMapping("/choices")
    public ApiResponse<String> chooseInitial(
            @CurrentUser User user,
            @Valid @RequestBody InitialChoiceRequest request
    ) {
        initialService.applyUserInitialChoices(user.getId(), request);
        return ApiResponse.ok("초기 선호 반영이 완료되었습니다.");
    }

}
