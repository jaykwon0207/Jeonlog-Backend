package com.jeonlog.exhibition_recommender.auth.service.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.dto.mobile.GoogleMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleMobileAuthService {

    private final Environment env;
    private final ObjectMapper objectMapper;
    private final OAuthLoginSuccessService successService;

    public OAuthLoginSuccessService.Result loginWithPkce(GoogleMobileLoginRequest request) throws Exception {
        String tokenUri = required("spring.security.oauth2.client.provider.google.token-uri");
        String userInfoUri = required("spring.security.oauth2.client.provider.google.user-info-uri");
        String clientId = required("spring.security.oauth2.client.registration.google.client-id");
        String clientSecret = required("spring.security.oauth2.client.registration.google.client-secret");

        String body = "grant_type=authorization_code"
                + "&code=" + enc(request.getAuthorizationCode())
                + "&client_id=" + enc(clientId)
                + "&client_secret=" + enc(clientSecret)
                + "&redirect_uri=" + enc(request.getRedirectUri())
                + "&code_verifier=" + enc(request.getCodeVerifier());

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> tokenResponse = HttpClient.newHttpClient()
                .send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());
        String accessToken = tokenJson.path("access_token").asText(null);
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("GOOGLE_TOKEN_EXCHANGE_FAILED");
        }

        HttpRequest userInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create(userInfoUri))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> userInfoResponse = HttpClient.newHttpClient()
                .send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode userJson = objectMapper.readTree(userInfoResponse.body());
        String email = userJson.path("email").asText(null);
        String name = userJson.path("name").asText("Google User");

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("GOOGLE_USERINFO_EMAIL_MISSING");
        }

        return successService.handle("GOOGLE", email, email, name, false);
    }

    private String required(String key) {
        String value = env.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
