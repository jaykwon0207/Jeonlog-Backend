package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.recommendation.dto.InitialChoiceRequest;
import com.jeonlog.exhibition_recommender.recommendation.dto.InitialExhibitionDto;
import com.jeonlog.exhibition_recommender.recommendation.service.InitialRecommendationService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/initial")
public class InitialRecommendationController {

    private final InitialRecommendationService initialService;
    private final UserRepository userRepository;

    @GetMapping("/recommendations")
    public ApiResponse<List<InitialExhibitionDto>> listInitial() {
        return ApiResponse.ok(initialService.listInitialExhibitions());
    }

    //사용자가 선택 완료 시 호출
    @PostMapping("/choices")
    public ApiResponse<String> chooseInitial(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody InitialChoiceRequest request
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        initialService.applyUserInitialChoices(user.getId(), request);
        return ApiResponse.ok("초기 선호 반영이 완료되었습니다.");
    }

}
