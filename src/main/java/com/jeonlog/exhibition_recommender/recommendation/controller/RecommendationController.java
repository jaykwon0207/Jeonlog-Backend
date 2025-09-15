package com.jeonlog.exhibition_recommender.recommendation.controller;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.recommendation.service.RecommendationService;
import com.jeonlog.exhibition_recommender.recommendation.dto.RecommendationDto;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDto>> getRecommendations(
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Exhibition> recs = recommendationService.recommend(user.getId());

        List<RecommendationDto> body = recs.stream()
                .map(RecommendationDto::from)
                .toList();

        return ResponseEntity.ok(body);
    }

    //연령대 별 전시 추천
    @GetMapping("/recommendations/age/{id}")
    public ResponseEntity<List<RecommendationDto>> getAgeRecommendations(
            @PathVariable("id") int ageId,
            @AuthenticationPrincipal String email
    ) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        var recs = recommendationService.recommendByAgeId(ageId);
        var body = recs.stream().map(RecommendationDto::from).toList();
        return ResponseEntity.ok(body);
    }

    //성별 별 전시 추천
    @GetMapping("/recommendations/gender/{id}")
    public ResponseEntity<List<RecommendationDto>> getGenderRecommendations(
            @PathVariable("id") int id,                 //0->남자, 1->여자
            @AuthenticationPrincipal String email
    ) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Gender gender = toGenderBinary(id);
        var recs = recommendationService.recommendByGender(gender);
        var body = recs.stream().map(RecommendationDto::from).toList();
        return ResponseEntity.ok(body);
    }

    private Gender toGenderBinary(int id) {
        if (id == 0) return Gender.MALE;              // 0 → 남자
        if (id == 1) return Gender.FEMALE;            // 1 → 여자
        throw new IllegalArgumentException("성별 id는 0(남) 또는 1(여)만 허용");
    }

}
