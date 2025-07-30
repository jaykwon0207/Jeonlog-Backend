package com.jeonlog.exhibition_recommender.oauth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2AuthenticationRedirectException extends OAuth2AuthenticationException {

    private final String redirectUrl;

    public OAuth2AuthenticationRedirectException(String redirectUrl) {
        super("리디렉션 필요: " + redirectUrl);
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
