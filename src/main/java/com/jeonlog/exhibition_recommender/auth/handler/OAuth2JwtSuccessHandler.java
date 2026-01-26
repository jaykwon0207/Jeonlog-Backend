package com.jeonlog.exhibition_recommender.auth.handler;

import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthLoginSuccessService successService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        try {
            OAuth2User user = (OAuth2User) authentication.getPrincipal();

            String provider = (String) user.getAttributes().get("provider");
            String oauthId  = (String) user.getAttributes().get("oauthId");
            String email    = (String) user.getAttributes().get("email");
            String name     = (String) user.getAttributes().get("name");

            boolean isNewUser = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("NEW_USER"));

            var result = successService.handle(
                    provider, oauthId, email, name, isNewUser
            );

            if (result.newUser()) {
                response.sendRedirect(
                        "jeonlogfront://onboarding/age?tempToken=" + result.tempToken()
                );
            } else {
                response.sendRedirect(
                        "jeonlogfront://auth"
                                + "?accessToken=" + result.accessToken()
                                + "&refreshToken=" + result.refreshToken()
                                + "&newUser=false"
                );
            }

        } catch (Exception e) {
            log.error("OAuth success handling failed", e);
            throw new RuntimeException(e);
        }
    }
}