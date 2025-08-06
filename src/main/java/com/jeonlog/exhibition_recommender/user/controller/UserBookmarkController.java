package com.jeonlog.exhibition_recommender.user.controller;

import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.dto.UserBookmarkDto;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserBookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserBookmarkController {

    private final UserBookmarkService bookmarkService;
    private final UserRepository userRepository;

    @PostMapping("/exhibitions/{id}/bookmark")
    public ResponseEntity<Map<String, String>> addBookmark(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        bookmarkService.addBookmark(exhibitionId, user);

        return ResponseEntity.ok(Map.of("message", "전시가 찜 목록에 추가되었습니다."));
    }


    @DeleteMapping("/exhibitions/{id}/bookmark")
    public ResponseEntity<Map<String, String>> cancelBookmark(
            @PathVariable("id") Long exhibitionId,
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        bookmarkService.cancelBookmark(exhibitionId, user);

        return ResponseEntity.ok(Map.of("message", "전시 찜이 취소되었습니다."));
    }

    @GetMapping("/users/bookmarks")
    public ResponseEntity<List<UserBookmarkDto>> getMyBookmarks(
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<UserBookmarkDto> response = bookmarkService.getBookmarksByUser(user);
        return ResponseEntity.ok(response);
    }


}
