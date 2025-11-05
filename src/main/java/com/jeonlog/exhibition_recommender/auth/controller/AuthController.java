package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
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
            String newAccess = jwtTokenProvider.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.ok(newAccess));
        } catch (JwtException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("INVALID_REFRESH", "리프레시 토큰이 만료되었거나 유효하지 않습니다."));
        }
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        // refresh_token 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(isProd)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.ok("로그아웃 완료"));
    }
}