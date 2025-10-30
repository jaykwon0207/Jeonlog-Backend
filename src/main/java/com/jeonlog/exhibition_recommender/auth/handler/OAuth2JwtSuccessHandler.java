package com.jeonlog.exhibition_recommender.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final Environment env;

    @Value("${app.frontend.redirect-base-url}")
    private String redirectBaseUrl; // 예: https://jeonlogfront://callback or RN 딥링크 기본 주소

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // ✅ 신규 회원일 경우 (세션에 tempOAuthAttributes가 있으면)
        if (request.getSession().getAttribute("tempOAuthAttributes") != null) {
            log.info("🆕 신규 회원 감지 → 온보딩 시작 페이지로 리다이렉트");

            // RN 딥링크 (React Native 앱에서 첫 온보딩 페이지)
            String onboardingUrl = "jeonlogfront://onboarding/age";
            response.sendRedirect(onboardingUrl);
            return;
        }

        // ✅ 기존 회원일 경우
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            log.error("❌ OAuth2 email is null. attributes={}", oAuth2User.getAttributes());
            response.sendRedirect("/login?error=EMAIL_NULL");
            
            return;
        }

        // JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 현재 실행 프로필 확인
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        // Refresh Token → HttpOnly 쿠키에 저장
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(isProd) // prod에서는 HTTPS 전용
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // ✅ 운영 환경: RN 딥링크로 리다이렉트 (토큰 전달)
        if (isProd) {
            String redirectUrl = redirectBaseUrl + "?token=" + accessToken;
            response.sendRedirect(redirectUrl);
            log.info("[PROD] OAuth2 login success → Redirected to {}", redirectUrl);
        }
        // ✅ 로컬 환경: JSON으로 응답 (Postman 테스트 등)
        else {
            response.setContentType("application/json");
            Map<String, Object> data = new HashMap<>();
            data.put("newUser", false);
            data.put("accessToken", accessToken);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.ok(data);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

            // 토큰 앞부분만 로깅 (보안)
            log.info("[LOCAL] OAuth2 login success for user={}, tokenPrefix={}",
                    email, accessToken.substring(0, Math.min(10, accessToken.length())) + "...");
        }
    }
}