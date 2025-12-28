package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private final Environment env;

    // ✅ Access Token 재발급 (웹 + 모바일 겸용)
    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<?>> issueAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            @RequestBody(required = false) Map<String, String> body
    ) {

        // 모바일일 경우 body로 전달됨
        String refreshToken = (body != null && body.get("refreshToken") != null)
                ? body.get("refreshToken")
                : refreshCookie;

        if (refreshToken == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("NO_REFRESH", "리프레시 토큰이 없습니다."));
        }

        try {
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email).orElseThrow(()-> new JwtException("user not found"));
            String newAccess = jwtTokenProvider.refreshAccessToken(refreshToken, user);
            return ResponseEntity.ok(ApiResponse.ok(newAccess));
        } catch (JwtException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("INVALID_REFRESH", "리프레시 토큰이 만료되었거나 유효하지 않습니다."));
        }
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        // 프론트에서는 단순히 로컬의 토큰을 삭제하면 되므로
        // 서버 측에서는 상태 유지나 쿠키 삭제 불필요
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 완료 — 토큰 무효화는 클라이언트 측에서 처리하세요."));
    }
}