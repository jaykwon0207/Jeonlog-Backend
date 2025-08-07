package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserLikeDto;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserLikeController {
    private final UserLikeService userLikeService;
    private final UserRepository userRepository;

    @PostMapping("/exhibitions/{id}/like")
    public ResponseEntity<Map<String, String>> addLike(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userLikeService.addLike(exhibitionId, user);

        return ResponseEntity.ok(Map.of("message", "전시에 좋아요를 눌렀습니다."));
    }

    @DeleteMapping("/exhibitions/{id}/like")
    public ResponseEntity<Map<String, String>> cancelLike(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        userLikeService.cancelLike(exhibitionId, user);

        return ResponseEntity.ok(Map.of("message", "좋아요가 취소되었습니다."));
    }

    @GetMapping("/users/likes")
    public ResponseEntity<List<UserLikeDto>> getMyLikes(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<UserLikeDto> response = userLikeService.getLikesByUser(user);
        return ResponseEntity.ok(response);
    }
}
