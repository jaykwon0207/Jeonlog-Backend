package com.jeonlog.exhibition_recommender.like.controller;

import com.jeonlog.exhibition_recommender.like.dto.RecordLikeDto;
import com.jeonlog.exhibition_recommender.like.service.RecordLikeService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecordLikeController {

    private final RecordLikeService recordLikeService;
    private final UserRepository userRepository;

    @PostMapping("/records/{id}/like")
    public ResponseEntity<Map<String, String>> like(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        recordLikeService.like(id, user);
        return ResponseEntity.ok(Map.of("message", "전시기록에 좋아요를 눌렀습니다."));
    }

    @DeleteMapping("/records/{id}/like")
    public ResponseEntity<Map<String, String>> unlike(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        recordLikeService.unlike(id, user);
        return ResponseEntity.ok(Map.of("message", "전시기록 좋아요가 취소되었습니다."));
    }

    @GetMapping("/users/record-likes")
    public ResponseEntity<List<RecordLikeDto>> myLikes(
            @AuthenticationPrincipal String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<RecordLikeDto> response = recordLikeService.getMyLiked(user);
        return ResponseEntity.ok(response);
    }
}
