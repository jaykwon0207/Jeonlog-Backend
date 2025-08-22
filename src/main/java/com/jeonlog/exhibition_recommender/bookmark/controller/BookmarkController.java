// bookmark/controller/BookmarkController.java
package com.jeonlog.exhibition_recommender.bookmark.controller;

import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkRequest;
import com.jeonlog.exhibition_recommender.bookmark.dto.BookmarkResponse;
import com.jeonlog.exhibition_recommender.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibitions/{exhibitionId}/bookmarks")
public class BookmarkController {

    private final BookmarkService service;

    // 북마크 추가 (알림여부 포함)
    @PostMapping
    public ResponseEntity<BookmarkResponse> add(@PathVariable Long exhibitionId,
                                                @AuthenticationPrincipal String email,
                                                @RequestBody(required = false) BookmarkRequest req) {
        return ResponseEntity.ok(service.add(exhibitionId, email, req));
    }

    // 북마크 알림여부 변경
    @PatchMapping("/notify")
    public ResponseEntity<BookmarkResponse> updateNotify(@PathVariable Long exhibitionId,
                                                         @AuthenticationPrincipal String email,
                                                         @RequestBody BookmarkRequest req) {
        return ResponseEntity.ok(service.updateNotify(exhibitionId, email, req));
    }

    // 북마크 삭제
    @DeleteMapping
    public ResponseEntity<BookmarkResponse> remove(@PathVariable Long exhibitionId,
                                                   @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(service.remove(exhibitionId, email));
    }

    // 전시 북마크 수
    @GetMapping("/count")
    public ResponseEntity<Long> count(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(service.count(exhibitionId));
    }


    // 내가 북마크한 전시 목록
    @GetMapping("/mine/list")
    public ResponseEntity<?> myBookmarks(@AuthenticationPrincipal String email,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listMyBookmarks(email, PageRequest.of(page, size)));
    }
}