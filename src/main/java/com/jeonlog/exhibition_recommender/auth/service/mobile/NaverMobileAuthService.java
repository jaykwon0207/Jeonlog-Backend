package com.jeonlog.exhibition_recommender.auth.service.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.dto.mobile.NaverMobileLoginRequest;
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
public class NaverMobileAuthService {

    private final Environment env;
    private final ObjectMapper objectMapper;
    private final OAuthLoginSuccessService successService;

    public OAuthLoginSuccessService.Result loginWithPkce(NaverMobileLoginRequest request) throws Exception {
        String tokenUri = required("spring.security.oauth2.client.provider.naver.token-uri");
        String userInfoUri = required("spring.security.oauth2.client.provider.naver.user-info-uri");
        String clientId = required("spring.security.oauth2.client.registration.naver.client-id");
        String clientSecret = required("spring.security.oauth2.client.registration.naver.client-secret");

        StringBuilder body = new StringBuilder();
        body.append("grant_type=authorization_code")
                .append("&code=").append(enc(request.getAuthorizationCode()))
                .append("&client_id=").append(enc(clientId))
                .append("&client_secret=").append(enc(clientSecret))
                .append("&redirect_uri=").append(enc(request.getRedirectUri()))
                .append("&code_verifier=").append(enc(request.getCodeVerifier()));

        if (request.getState() != null && !request.getState().isBlank()) {
            body.append("&state=").append(enc(request.getState()));
        }

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create(tokenUri))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> tokenResponse = HttpClient.newHttpClient()
                .send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());
        String accessToken = tokenJson.path("access_token").asText(null);
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("NAVER_TOKEN_EXCHANGE_FAILED");
        }

        HttpRequest userInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create(userInfoUri))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> userInfoResponse = HttpClient.newHttpClient()
                .send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

        JsonNode responseNode = objectMapper.readTree(userInfoResponse.body()).path("response");
        String naverId = responseNode.path("id").asText(null);
        String email = responseNode.path("email").asText(null);
        String name = responseNode.path("name").asText("Naver User");

        if (naverId == null || naverId.isBlank()) {
            throw new IllegalStateException("NAVER_USERINFO_ID_MISSING");
        }

        return successService.handle("NAVER", naverId, email, name, false);
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
