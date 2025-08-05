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


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // ✅ 신규 사용자일 경우: /oauth/add-info로 리디렉트
        if (request.getSession().getAttribute("tempOAuthAttributes") != null) {
            log.info("🆕 신규 사용자 - 추가 정보 입력 필요. /oauth/add-info로 리디렉트");
            response.sendRedirect("/oauth/add-info");
            return;
        }

        // ✅ 기존 사용자: JWT 발급 후 리디렉트
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("🔐 로그인한 사용자 이메일: {}", email);

        String token = jwtTokenProvider.createToken(email);
        String redirectUrl = "http://localhost:3000/oauth2/redirect?token=" + token;
        log.info("➡️ 프론트엔드 리디렉트 URL: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}