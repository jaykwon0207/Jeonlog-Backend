package com.jeonlog.exhibition_recommender.auth.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2AuthenticationRedirectException extends OAuth2AuthenticationException {

    private final String redirectUrl;

    public OAuth2AuthenticationRedirectException(String redirectUrl) {
        super("Redirect for additional OAuth2 info");
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
