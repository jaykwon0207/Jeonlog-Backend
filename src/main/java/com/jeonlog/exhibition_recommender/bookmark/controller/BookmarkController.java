package com.jeonlog.exhibition_recommender.bookmark.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkRequest;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkResponse;
import com.jeonlog.exhibition_recommender.bookmark.service.BookmarkService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions")
public class BookmarkController {

    private final BookmarkService service;

    // 북마크 추가 (알림여부 포함)
    @PostMapping("/{exhibitionId}/bookmarks")
    public ApiResponse<BookmarkResponse> add(
            @PathVariable Long exhibitionId,
            @CurrentUser User user,
            @RequestBody(required = false) BookmarkRequest req) {
        return ApiResponse.ok(service.add(exhibitionId, user.getEmail(), req));
    }

    // 북마크 알림여부 변경
    @PatchMapping("/{exhibitionId}/bookmarks/notify")
    public ApiResponse<BookmarkResponse> updateNotify(
            @PathVariable Long exhibitionId,
            @CurrentUser User user,
            @RequestBody BookmarkRequest req) {
        return ApiResponse.ok(service.updateNotify(exhibitionId, user.getEmail(), req));
    }

    // 북마크 삭제
    @DeleteMapping("/{exhibitionId}/bookmarks")
    public ApiResponse<BookmarkResponse> remove(
            @PathVariable Long exhibitionId,
            @CurrentUser User user) {
        return ApiResponse.ok(service.remove(exhibitionId, user.getEmail()));
    }

    // 전시 북마크 수
    @GetMapping("/{exhibitionId}/bookmarks/count")
    public ApiResponse<Long> count(@PathVariable Long exhibitionId) {
        return ApiResponse.ok(service.count(exhibitionId));
    }

    // 내가 북마크한 전시 목록
    @GetMapping("/bookmarks/mine/list")
    public ApiResponse<?> myBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = userDetails.getUsername();
        return ApiResponse.ok(service.listMyBookmarks(email, PageRequest.of(page, size)));
    }
}