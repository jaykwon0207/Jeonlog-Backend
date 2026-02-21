package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.BlockActionResponse;
import com.jeonlog.exhibition_recommender.user.dto.BlockedUserDto;
import com.jeonlog.exhibition_recommender.user.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/{targetUserId}/block")
    public ApiResponse<BlockActionResponse> block(
            @CurrentUser User user,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.ok(userBlockService.block(user, targetUserId));
    }

    @DeleteMapping("/{targetUserId}/block")
    public ApiResponse<BlockActionResponse> unblock(
            @CurrentUser User user,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.ok(userBlockService.unblock(user, targetUserId));
    }

    @GetMapping("/blocks")
    public ApiResponse<List<BlockedUserDto>> getBlockedUsers(@CurrentUser User user) {
        return ApiResponse.ok(userBlockService.getBlockedUsers(user));
    }
}
