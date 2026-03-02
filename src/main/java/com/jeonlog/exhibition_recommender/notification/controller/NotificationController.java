package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.auth.annotation.CurrentUser;
import com.jeonlog.exhibition_recommender.notification.dto.NotificationListResponse;
import com.jeonlog.exhibition_recommender.notification.dto.UnreadCountResponse;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<NotificationListResponse> list(
            @CurrentUser User user,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(user.getId(), cursor, size));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> read(@CurrentUser User user, @PathVariable Long id) {
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(@CurrentUser User user) {
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.unreadCount(user.getId())));
    }
}
