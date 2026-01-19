package com.jeonlog.exhibition_recommender.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String provider = (String) oAuth2User.getAttributes().get("provider");
        String oauthId = (String) oAuth2User.getAttributes().get("id");
        String email = (String) oAuth2User.getAttributes().get("email");

        boolean isNewUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("NEW_USER"));

        if (isNewUser) {
            String json = objectMapper.writeValueAsString(
                    oAuth2User.getAttributes()
            );
            String base64 = Base64.getUrlEncoder().encodeToString(json.getBytes());
            String tempToken = jwtTokenProvider.createTempToken(base64, 60 * 60 * 1000);

            response.sendRedirect(
                    "jeonlogfront://onboarding/age?tempToken=" + tempToken
            );
            return;
        }

        User user = userRepository
                .findByOauthProviderAndOauthId(
                        OauthProvider.valueOf(provider),
                        oauthId
                )
                .orElseThrow(() -> new IllegalStateException("user not found"));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        response.sendRedirect(
                "jeonlogfront://auth?accessToken=" + accessToken
                        + "&refreshToken=" + refreshToken
                        + "&newUser=false"
        );
    }
}