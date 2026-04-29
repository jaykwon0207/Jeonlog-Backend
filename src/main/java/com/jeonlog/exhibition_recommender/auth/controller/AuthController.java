package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.ReviewerLoginRequest;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    @Value("${review-login.enabled:false}")
    private boolean reviewLoginEnabled;
    @Value("${review-login.code:}")
    private String reviewLoginCode;
    @Value("${review-login.oauth-id:app-reviewer}")
    private String reviewLoginOauthId;
    @Value("${review-login.name:App Reviewer}")
    private String reviewLoginName;

    // ✅ Access Token 재발급 (웹 + 모바일 공용)
    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<?>> issueAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            @RequestBody(required = false) Map<String, String> body
    ) {
        log.info("[AUTH] refresh_start endpoint=/api/auth/access-token");

        // 모바일 → body / 웹 → cookie
        String refreshToken = (body != null && body.get("refreshToken") != null)
                ? body.get("refreshToken")
                : refreshCookie;

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("[AUTH] refresh_failed code=NO_REFRESH reason=missing_refresh_token endpoint=/api/auth/access-token");
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
            log.info("[AUTH] refresh_success userId={} provider={} endpoint=/api/auth/access-token",
                    user.getId(),
                    provider);

            return ResponseEntity.ok(ApiResponse.ok(newAccessToken));

        } catch (JwtException e) {
            log.warn("[AUTH] refresh_failed code=INVALID_REFRESH reason={} endpoint=/api/auth/access-token", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(
                            "INVALID_REFRESH",
                            "리프레시 토큰이 만료되었거나 유효하지 않습니다."
                    ));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        // 서버는 stateless
        // 프론트에서 토큰 삭제로 처리
        return ResponseEntity.ok(
                ApiResponse.ok("로그아웃 완료 — 클라이언트에서 토큰을 삭제하세요.")
        );
    }

    // 앱 심사 대응용 더미 로그인 (이스터에그 진입 후 코드 입력)
    @PostMapping("/reviewer-login")
    public ResponseEntity<ApiResponse<?>> reviewerLogin(
            @RequestBody ReviewerLoginRequest request
    ) {
        log.info("[AUTH] reviewer_login_start endpoint=/api/auth/reviewer-login");
        if (!reviewLoginEnabled) {
            log.warn("[AUTH] reviewer_login_failed code=NOT_FOUND reason=review_login_disabled");
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("NOT_FOUND", "지원하지 않는 경로입니다."));
        }

        if (!StringUtils.hasText(reviewLoginCode)) {
            log.warn("[AUTH] reviewer_login_failed code=REVIEW_LOGIN_NOT_CONFIGURED");
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("REVIEW_LOGIN_NOT_CONFIGURED", "심사용 로그인 설정이 없습니다."));
        }

        String reviewCode = request != null ? request.getEffectiveReviewCode() : null;
        if (!reviewLoginCode.equals(reviewCode)) {
            log.warn("[AUTH] reviewer_login_failed code=INVALID_REVIEW_CODE");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("INVALID_REVIEW_CODE", "심사용 코드가 올바르지 않습니다."));
        }

        User reviewer = userRepository.findByOauthProviderAndOauthId(OauthProvider.APPLE, reviewLoginOauthId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(reviewLoginName)
                                .oauthProvider(OauthProvider.APPLE)
                                .oauthId(reviewLoginOauthId)
                                .role(com.jeonlog.exhibition_recommender.user.domain.Role.USER)
                                .build()
                ));

        String accessToken = jwtTokenProvider.createAccessToken(reviewer);
        String refreshToken = jwtTokenProvider.createRefreshToken(reviewer);
        log.info("[AUTH] reviewer_login_success userId={} provider={} endpoint=/api/auth/reviewer-login",
                reviewer.getId(),
                reviewer.getOauthProvider());

        return ResponseEntity.ok(
                ApiResponse.ok(Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "newUser", false,
                        "reviewer", true
                ))
        );
    }
}
