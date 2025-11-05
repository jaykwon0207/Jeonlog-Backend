package com.jeonlog.exhibition_recommender.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final Environment env;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // ✅ 신규 회원일 경우 → 온보딩 화면으로 딥링크 이동
        if (request.getSession().getAttribute("tempOAuthAttributes") != null) {
            log.info("🆕 신규 회원 → 온보딩 페이지로 이동");
            response.sendRedirect("jeonlogfront://onboarding/age");
            return;
        }

        // ✅ 기존 회원
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            log.error("❌ OAuth2 email is null. attributes={}", oAuth2User.getAttributes());
            response.sendRedirect("/login?error=EMAIL_NULL");
            return;
        }

        // ✅ JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // ✅ RN 딥링크 리다이렉트
        String redirectUrl = String.format(
                "jeonlogfront://auth?accessToken=%s&refreshToken=%s&newUser=false",
                accessToken, refreshToken
        );

        log.info("✅ OAuth2 login success for user={} → redirect: {}", email, redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}