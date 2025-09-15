package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.dto.SimpleUserProfileDto;
import com.jeonlog.exhibition_recommender.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    // 🔹 팔로잉 목록
    @GetMapping("/followings")
    public ApiResponse<List<SimpleUserProfileDto>> getFollowings(@AuthenticationPrincipal String email) {
        return ApiResponse.ok(profileService.getFollowings(email));
    }

    // 🔹 팔로워 목록
    @GetMapping("/followers")
    public ApiResponse<List<SimpleUserProfileDto>> getFollowers(@AuthenticationPrincipal String email) {
        return ApiResponse.ok(profileService.getFollowers(email));
    }

    // 🔹 팔로우
    @PostMapping("/{targetId}/follow")
    public ApiResponse<String> follow(@AuthenticationPrincipal String email,
                                      @PathVariable Long targetId) {
        profileService.follow(email, targetId);
        return ApiResponse.ok("팔로우 했습니다.");
    }

    // 🔹 언팔로우
    @DeleteMapping("/{targetId}/unfollow")
    public ApiResponse<String> unfollow(@AuthenticationPrincipal String email,
                                        @PathVariable Long targetId) {
        profileService.unfollow(email, targetId);
        return ApiResponse.ok("언팔로우 했습니다.");
    }

    // 🔹 다른 사람 프로필 조회
    @GetMapping("/{userId}/profile")
    public ApiResponse<SimpleUserProfileDto> getUserProfile(
            @AuthenticationPrincipal String myEmail,
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(profileService.getUserProfile(myEmail, userId));
    }
}