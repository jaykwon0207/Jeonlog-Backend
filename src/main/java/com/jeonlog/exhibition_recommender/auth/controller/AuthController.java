package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<?>> issueAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("INVALID_REFRESH", "리프레시 토큰이 유효하지 않습니다.")
            );
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String newAccess = jwtTokenProvider.createAccessToken(email);

        return ResponseEntity.ok(
                ApiResponse.ok(newAccess)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // refresh 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("logged out");
    }
}