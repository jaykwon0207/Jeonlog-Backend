package com.jeonlog.exhibition_recommender.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider; // ✅ 추가
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");
        String provider = (String) oAuth2User.getAttributes().getOrDefault("provider", "GOOGLE");
        String oauthId = (String) oAuth2User.getAttributes().getOrDefault("id", "");

        if (email == null) {
            response.sendRedirect("/login?error=NO_EMAIL");
            return;
        }

        // ✅ 신규 사용자 감지
        boolean isNewUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("NEW_USER"));

        if (isNewUser) {
            log.info("🆕 신규 사용자 감지 → RN 온보딩으로 tempToken 전달");

            // ✅ 1️⃣ OAuthAttributes 객체 생성
            OAuthAttributes oAuthAttributes = OAuthAttributes.builder()
                    .email(email)
                    .name(name)
                    .oauthProvider(OauthProvider.valueOf(provider.toUpperCase()))
                    .oauthId(oauthId)
                    .build();

            // ✅ 2️⃣ JSON → Base64 변환
            String json = objectMapper.writeValueAsString(oAuthAttributes);
            String base64 = Base64.getUrlEncoder().encodeToString(json.getBytes());

            // ✅ 3️⃣ tempToken (유효기간: 1시간)
            String tempToken = jwtTokenProvider.createTempToken(base64, 60 * 60 * 1000);

            // ✅ 4️⃣ RN 딥링크로 전달
            String redirectUrl = String.format("jeonlogfront://onboarding/age?tempToken=%s", tempToken);
            log.info("🪶 Redirect URL: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }

        // ✅ 기존 사용자면 바로 access/refresh 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        String redirectUrl = String.format(
                "jeonlogfront://auth?accessToken=%s&refreshToken=%s&newUser=false",
                accessToken, refreshToken
        );

        log.info("✅ 기존 사용자 로그인 성공 → redirect: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}