package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.user.dto.SimpleUserProfileDto;
import com.jeonlog.exhibition_recommender.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;


    //팔로잉 확인
    @GetMapping("/followings")
    public ResponseEntity<List<SimpleUserProfileDto>> getFollowings(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(profileService.getFollowings(email));
    }

    //팔로워 확인
    @GetMapping("/followers")
    public ResponseEntity<List<SimpleUserProfileDto>> getFollowers(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(profileService.getFollowers(email));
    }


    // 팔로우
    @PostMapping("/{targetId}/follow")
    public ResponseEntity<?> follow(@AuthenticationPrincipal String email, @PathVariable Long targetId) {
        profileService.follow(email, targetId);
        return ResponseEntity.ok(Map.of("message", "팔로우 했습니다."));
    }

    // 언팔
    @DeleteMapping("/{targetId}/unfollow")
    public ResponseEntity<?> unfollow(@AuthenticationPrincipal String email, @PathVariable Long targetId) {
        profileService.unfollow(email, targetId);
        return ResponseEntity.ok(Map.of("message", "언팔로우 했습니다."));
    }

    //다른 사람 프로필 확인
    @GetMapping("/{userId}/profile")
    public ResponseEntity<SimpleUserProfileDto> getUserProfile(
            @AuthenticationPrincipal String myEmail,
            @PathVariable Long userId
    ) {
        SimpleUserProfileDto profile = profileService.getUserProfile(myEmail, userId);
        return ResponseEntity.ok(profile);
    }


}