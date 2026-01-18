package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.notification.dto.NotificationListResponse;
import com.jeonlog.exhibition_recommender.notification.dto.UnreadCountResponse;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // TODO: 지금은 userId를 파라미터로 받는데, 너희 auth 붙어있으면 "내 userId"로 바꾸면 됨
    @GetMapping
    public ResponseEntity<NotificationListResponse> list(
            @RequestParam Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, cursor, size));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> read(@RequestParam Long userId, @PathVariable Long id) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.unreadCount(userId)));
    }
}
