package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUserEmail;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.VisitRequest;
import com.jeonlog.exhibition_recommender.user.dto.VisitedExhibitionDto;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserVisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserVisitController {

    private final UserVisitService userVisitService;
    private final UserRepository userRepository;

    // ✅ 전시 방문 기록 저장
    @PostMapping("/exhibitions/{id}/visit")
    public ApiResponse<Void> recordVisit(
            @PathVariable Long id,
            @RequestBody(required = false) VisitRequest request,
            @CurrentUserEmail String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userVisitService.recordVisit(id, email, request);

        return ApiResponse.ok(null); // ✅ data 없음
    }

    // ✅ 내가 방문한 전시 목록
    @GetMapping("/users/visits")
    public ApiResponse<List<VisitedExhibitionDto>> getVisitedExhibitions(
            @CurrentUserEmail String email) {

        List<VisitedExhibitionDto> visited = userVisitService.getVisitedExhibitions(email);

        return ApiResponse.ok(visited);
    }
}