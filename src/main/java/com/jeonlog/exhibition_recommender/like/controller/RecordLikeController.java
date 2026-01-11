package com.jeonlog.exhibition_recommender.like.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
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

    @PostMapping("/records/{id}/like")
    public ApiResponse<RecordLikeDto> like(
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return ApiResponse.ok(recordLikeService.like(id, user));
    }

    @DeleteMapping("/records/{id}/like")
    public ApiResponse<RecordLikeDto> unlike(
            @PathVariable("id") Long id,
            @CurrentUser User user
    ) {
        return ApiResponse.ok(recordLikeService.unlike(id, user));
    }

    @GetMapping("/users/record-likes")
    public ApiResponse<List<RecordLikeDto>> myLikes(
            @CurrentUser User user
    ) {
        return ApiResponse.ok(recordLikeService.getMyLiked(user));
    }
}