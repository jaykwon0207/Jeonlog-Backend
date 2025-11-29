package com.jeonlog.exhibition_recommender.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
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
        String provider = (String) oAuth2User.getAttributes().get("provider");
        String oauthId = (String) oAuth2User.getAttributes().get("id");

        if (email == null || provider == null) {
            response.sendRedirect("/login?error=INVALID_OAUTH_DATA");
            return;
        }

        boolean isNewUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("NEW_USER"));

        // 신규 사용자 처리
        if (isNewUser) {
            OAuthAttributes attributes = OAuthAttributes.builder()
                    .email(email)
                    .name(name)
                    .oauthProvider(OauthProvider.valueOf(provider))
                    .oauthId(oauthId)
                    .build();

            String json = objectMapper.writeValueAsString(attributes);
            String base64 = Base64.getUrlEncoder().encodeToString(json.getBytes());
            String tempToken = jwtTokenProvider.createTempToken(base64, 60 * 60 * 1000);

            String redirectUrl = "jeonlogfront://onboarding/age?tempToken=" + tempToken;

            log.info("🆕 신규 사용자");
            log.info("➡️ redirect: {}", redirectUrl);

            response.sendRedirect(redirectUrl);
            return;
        }

        // 기존 사용자 처리
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        log.info("🔐 기존 사용자 로그인 성공");
        log.info("👉 email: {}", email);
        log.info("👉 accessToken: {}", accessToken);
        log.info("👉 refreshToken: {}", refreshToken);

        String redirectUrl = String.format(
                "jeonlogfront://auth?accessToken=%s&refreshToken=%s&newUser=false",
                accessToken, refreshToken
        );

        log.info("➡️ redirect: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}