package com.jeonlog.exhibition_recommender.auth.service;


import com.jeonlog.exhibition_recommender.auth.exception.OAuth2AuthenticationRedirectException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {

        if (exception instanceof OAuth2AuthenticationRedirectException redirectException) {
            response.sendRedirect(redirectException.getRedirectUrl());
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
