package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.auth.model.CustomUserDetails;
import com.jeonlog.exhibition_recommender.notification.dto.RegisterPushTokenRequest;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push-tokens")
public class PushTokenController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterPushTokenRequest req,
                                      Authentication authentication) {

        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        Long userId = cud.getUser().getId();

        notificationService.registerPushToken(userId, req.getToken(), req.getPlatform());
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}
