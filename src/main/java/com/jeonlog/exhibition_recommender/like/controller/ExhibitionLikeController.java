package com.jeonlog.exhibition_recommender.like.controller;

import com.jeonlog.exhibition_recommender.like.dto.LikeResponse;
import com.jeonlog.exhibition_recommender.like.service.ExhibitionLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions/{exhibitionId}/likes")
public class ExhibitionLikeController {

    private final ExhibitionLikeService service;

    // 좋아요 추가
    @PostMapping
    public ResponseEntity<LikeResponse> like(@PathVariable Long exhibitionId,
                                             @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(service.like(exhibitionId, email));
    }

    // 좋아요 취소
    @DeleteMapping
    public ResponseEntity<LikeResponse> unlike(@PathVariable Long exhibitionId,
                                               @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(service.unlike(exhibitionId, email));
    }

    // 좋아요 수 조회
    @GetMapping("/count")
    public ResponseEntity<Long> count(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(service.count(exhibitionId));
    }

    // 해당 전시에 좋아요한 사용자 목록
    @GetMapping("/users")
    public ResponseEntity<?> likedUsers(@PathVariable Long exhibitionId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                service.listUsers(exhibitionId, PageRequest.of(page, size))
        );
    }

    // 내가 좋아요한 전시 목록
    @GetMapping("/me")
    public ResponseEntity<?> myLiked(@AuthenticationPrincipal String email,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                service.listMyLikedExhibitions(email, PageRequest.of(page, size))
        );
    }
}
