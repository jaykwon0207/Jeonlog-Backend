package com.jeonlog.exhibition_recommender.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProviderAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegate;

    public ProviderAwareAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return customize(delegate.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest original) {
        if (original == null) {
            return null;
        }

        String registrationId = original.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        if (registrationId == null) {
            return original;
        }

        Map<String, Object> additionalParams = new HashMap<>(original.getAdditionalParameters());
        switch (registrationId.toLowerCase()) {
            case "google" -> additionalParams.put("prompt", "select_account");
            case "naver" -> additionalParams.put("auth_type", "reauthenticate");
            default -> {
                return original;
            }
        }

        return OAuth2AuthorizationRequest.from(original)
                .additionalParameters(additionalParams)
                .build();
    }
}
