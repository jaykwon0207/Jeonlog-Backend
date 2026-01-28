package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.notification.dto.NotificationListResponse;
import com.jeonlog.exhibition_recommender.notification.dto.UnreadCountResponse;
import com.jeonlog.exhibition_recommender.notification.service.NotificationService;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.exception.UserNotFoundException;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<NotificationListResponse> list(
            Authentication authentication,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long myUserId = resolveMyUserId(authentication);
        return ResponseEntity.ok(notificationService.getNotifications(myUserId, cursor, size));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> read(Authentication authentication, @PathVariable Long id) {
        Long myUserId = resolveMyUserId(authentication);
        notificationService.markAsRead(myUserId, id);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(Authentication authentication) {
        Long myUserId = resolveMyUserId(authentication);
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.unreadCount(myUserId)));
    }

    private Long resolveMyUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        // 1) JWT 필터에서 principal을 String(email)로 넣는 경우 (가장 흔함)
        if (principal instanceof String s) {
            email = s;
        }
        // 2) OAuth2 로그인 흐름에서 principal이 OAuth2User인 경우
        else if (principal instanceof OAuth2User oAuth2User) {
            Object e = oAuth2User.getAttributes().get("email");
            if (e != null) email = String.valueOf(e);
        }

        // fallback: authentication.getName()
        if (email == null || email.isBlank()) {
            String name = authentication.getName();
            if (name != null && !name.isBlank()) email = name;
        }

        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("이메일 정보를 찾을 수 없습니다.");
        }

        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("로그인 유저 없음"));
        return me.getId();
    }

    // controller 내부에서만 쓸 거면 이렇게 간단히 둬도 됨
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }
}
