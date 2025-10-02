package com.jeonlog.exhibition_recommender.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        response.setContentType("application/json");

        // 신규 회원 → 추가정보 페이지로 안내
        if (request.getSession().getAttribute("tempOAuthAttributes") != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("newUser", true);
            data.put("redirectTo", "/signup/add-info");

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.ok(data);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return;
        }

        // 기존 회원 → JWT 발급
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // Refresh Token을 HttpOnly 쿠키에 저장
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // JSON 응답
        Map<String, Object> data = new HashMap<>();
        data.put("newUser", false);
        data.put("accessToken", accessToken);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.ok(data);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}