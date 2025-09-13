package com.jeonlog.exhibition_recommender.auth.handler;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 🟢 현재는 EB 주소로 고정 (프론트 배포 전)
    private static final String EB_BASE_URL =
            "http://jeonlog-env.eba-qstxpqtg.ap-northeast-2.elasticbeanstalk.com";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 신규 사용자 → /oauth/add-info
        if (request.getSession().getAttribute("tempOAuthAttributes") != null) {
            log.info("🆕 신규 사용자 - 추가 정보 입력 필요 → /oauth/add-info");
            response.sendRedirect("/oauth/add-info");
            return;
        }

        // 기존 사용자 → JWT 발급
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            log.error("❌ OAuth2 email is null. attributes={}", oAuth2User.getAttributes());
            response.sendRedirect("/login?error=EMAIL_NULL");
            return;
        }

        try {
            String token = jwtTokenProvider.createToken(email);
            log.info("🔐 JWT 생성 완료 for {}", email);

            // 🟢 URL 파라미터로 토큰 전달
            String redirectUrl = EB_BASE_URL + "/oauth2/redirect?token=" + token;
            log.info("➡️ Redirect to {}", redirectUrl);

            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            log.error("❌ JWT 발급/리다이렉트 중 오류", ex);
            response.sendRedirect("/login?error=JWT_CREATE_FAILED");
        }
    }
}