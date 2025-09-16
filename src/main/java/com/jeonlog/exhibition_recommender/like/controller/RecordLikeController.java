package com.jeonlog.exhibition_recommender.like.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.like.dto.RecordLikeDto;
import com.jeonlog.exhibition_recommender.like.service.RecordLikeService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecordLikeController {

    private final RecordLikeService recordLikeService;
    private final UserRepository userRepository;

    @PostMapping("/records/{id}/like")
    public ApiResponse<RecordLikeDto> like(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return ApiResponse.ok(recordLikeService.like(id, user));
    }


    //전시기록 좋아요 취소
    @DeleteMapping("/records/{id}/like")
    public ApiResponse<RecordLikeDto> unlike(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return ApiResponse.ok(recordLikeService.unlike(id, user));
    }

    @GetMapping("/users/record-likes")
    public ApiResponse<List<RecordLikeDto>> myLikes(
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return ApiResponse.ok(recordLikeService.getMyLiked(user));
    }
}