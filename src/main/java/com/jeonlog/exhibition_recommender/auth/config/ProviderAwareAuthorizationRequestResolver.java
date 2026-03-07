package com.jeonlog.exhibition_recommender.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProviderAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String CODE_CHALLENGE = "code_challenge";
    private static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final int PKCE_VERIFIER_BYTE_SIZE = 64;

    private final OAuth2AuthorizationRequestResolver delegate;
    private final SecureRandom secureRandom = new SecureRandom();

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
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.from(original);
        switch (registrationId.toLowerCase()) {
            case "google" -> additionalParams.put("prompt", "select_account");
            case "naver" -> additionalParams.put("auth_type", "reauthenticate");
            default -> {
                return original;
            }
        }

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = createCodeChallenge(codeVerifier);

        additionalParams.put(CODE_CHALLENGE, codeChallenge);
        additionalParams.put(CODE_CHALLENGE_METHOD, "S256");

        Map<String, Object> attributes = new HashMap<>(original.getAttributes());
        attributes.put(CODE_VERIFIER, codeVerifier);

        return builder
                .additionalParameters(additionalParams)
                .attributes(attrs -> {
                    attrs.clear();
                    attrs.putAll(attributes);
                })
                .build();
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[PKCE_VERIFIER_BYTE_SIZE];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String createCodeChallenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
