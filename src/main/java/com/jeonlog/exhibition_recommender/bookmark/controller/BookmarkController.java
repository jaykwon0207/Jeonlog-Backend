package com.jeonlog.exhibition_recommender.bookmark.controller;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkRequest;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkResponse;
import com.jeonlog.exhibition_recommender.bookmark.service.BookmarkService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions/{exhibitionId}/bookmarks")
public class BookmarkController {

    private final BookmarkService service;

    // 북마크 추가 (알림여부 포함)
    @PostMapping
    public ApiResponse<BookmarkResponse> add(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) BookmarkRequest req) {
        String email = userDetails.getUsername(); // 또는 userDetails.getUser().getEmail()
        return ApiResponse.ok(service.add(exhibitionId, email, req));
    }

    // 북마크 알림여부 변경
    @PatchMapping("/notify")
    public ApiResponse<BookmarkResponse> updateNotify(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal String email,
            @RequestBody BookmarkRequest req) {
        return ApiResponse.ok(service.updateNotify(exhibitionId, email, req));
    }

    // 북마크 삭제
    @DeleteMapping
    public ApiResponse<BookmarkResponse> remove(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal String email) {
        return ApiResponse.ok(service.remove(exhibitionId, email));
    }

    // 전시 북마크 수
    @GetMapping("/count")
    public ApiResponse<Long> count(@PathVariable Long exhibitionId) {
        return ApiResponse.ok(service.count(exhibitionId));
    }

    // 내가 북마크한 전시 목록
    @GetMapping("/mine/list")
    public ApiResponse<?> myBookmarks(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.listMyBookmarks(email, PageRequest.of(page, size)));
    }
}