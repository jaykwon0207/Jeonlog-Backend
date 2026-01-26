package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // ✅ Access Token 재발급 (웹 + 모바일 공용)
    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<?>> issueAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            @RequestBody(required = false) Map<String, String> body
    ) {

        // 모바일 → body / 웹 → cookie
        String refreshToken = (body != null && body.get("refreshToken") != null)
                ? body.get("refreshToken")
                : refreshCookie;

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("NO_REFRESH", "리프레시 토큰이 없습니다."));
        }

        try {
            // ✅ subject = PROVIDER:OAUTH_ID
            String subject = jwtTokenProvider.getSubject(refreshToken);
            String[] parts = subject.split(":");

            if (parts.length != 2) {
                throw new JwtException("invalid subject");
            }

            OauthProvider provider = OauthProvider.valueOf(parts[0]);
            String oauthId = parts[1];

            User user = userRepository
                    .findByOauthProviderAndOauthId(provider, oauthId)
                    .orElseThrow(() -> new JwtException("user not found"));

            String newAccessToken =
                    jwtTokenProvider.refreshAccessToken(refreshToken, user);

            return ResponseEntity.ok(ApiResponse.ok(newAccessToken));

        } catch (JwtException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(
                            "INVALID_REFRESH",
                            "리프레시 토큰이 만료되었거나 유효하지 않습니다."
                    ));
        }
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        // 서버는 stateless
        // 프론트에서 토큰 삭제로 처리
        return ResponseEntity.ok(
                ApiResponse.ok("로그아웃 완료 — 클라이언트에서 토큰을 삭제하세요.")
        );
    }
}