package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.notification.dto.RegisterPushTokenRequest;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.exception.UserNotFoundException;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> register(
            Authentication authentication,
            @RequestBody RegisterPushTokenRequest req
    ) {
        Long myUserId = resolveMyUserId(authentication);

        notificationService.registerPushToken(
                myUserId,
                req.getToken(),
                req.getPlatform()
        );

        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    private Long resolveMyUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        String myEmail = authentication.getName();
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));

        return me.getId();
    }
}
