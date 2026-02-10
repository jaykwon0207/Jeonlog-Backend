package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.SimpleUserProfileDto;
import com.jeonlog.exhibition_recommender.user.service.ProfileService;
import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController { // ss

    private final ProfileService profileService;

    // 팔로잉 목록
    @GetMapping("/followings")
    public ApiResponse<List<SimpleUserProfileDto>> getFollowings(@CurrentUser User user) {
        return ApiResponse.ok(profileService.getFollowings(user.getEmail()));
    }

    // 팔로워 목록
    @GetMapping("/followers")
    public ApiResponse<List<SimpleUserProfileDto>> getFollowers(@CurrentUser User user) {
        return ApiResponse.ok(profileService.getFollowers(user.getEmail()));
    }

    // 팔로우
    @PostMapping("/{targetId}/follow")
    public ApiResponse<String> follow(@CurrentUser User user,
                                      @PathVariable Long targetId) {
        profileService.follow(user.getEmail(), targetId);
        return ApiResponse.ok("팔로우 했습니다.");
    }

    // 언팔로우
    @DeleteMapping("/{targetId}/unfollow")
    public ApiResponse<String> unfollow(@CurrentUser User user,
                                        @PathVariable Long targetId) {
        profileService.unfollow(user.getEmail(), targetId);
        return ApiResponse.ok("언팔로우 했습니다.");
    }

    // 다른 사람 프로필 조회
    @GetMapping("/{userId}/profile")
    public ApiResponse<SimpleUserProfileDto> getUserProfile(
            @CurrentUser User user,
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(profileService.getUserProfile(user.getEmail(), userId));
    }


    // 내 팔로잉 목록
    @GetMapping("/me/followings")
    public ApiResponse<List<SimpleUserProfileDto>> getMyFollowings(
            @CurrentUser User user
    ) {
        return ApiResponse.ok(
                profileService.getFollowings(user.getEmail())
        );
    }

    // 내 팔로워 목록
    @GetMapping("/me/followers")
    public ApiResponse<List<SimpleUserProfileDto>> getMyFollowers(
            @CurrentUser User user
    ) {
        return ApiResponse.ok(
                profileService.getFollowers(user.getEmail())
        );
    }



    // 다른 유저 팔로잉 목록
    @GetMapping("/{userId}/followings")
    public ApiResponse<List<SimpleUserProfileDto>> getUserFollowings(
            @CurrentUser User user,
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(
                profileService.getFollowingsByUserId(user.getEmail(), userId)
        );
    }

    // 다른 유저 팔로워 목록
    @GetMapping("/{userId}/followers")
    public ApiResponse<List<SimpleUserProfileDto>> getUserFollowers(
            @CurrentUser User user,
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(
                profileService.getFollowersByUserId(user.getEmail(), userId)
        );
    }
}