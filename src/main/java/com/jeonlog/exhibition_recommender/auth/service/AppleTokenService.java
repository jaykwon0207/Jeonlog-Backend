package com.jeonlog.exhibition_recommender.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleTokenService {

    private final Environment env;
    private final ObjectMapper objectMapper;

    public String exchangeCodeForIdToken(String authorizationCode) throws Exception {

        String clientId = env.getProperty("apple.client.id");
        String teamId = env.getProperty("apple.team.id");
        String keyId = env.getProperty("apple.key.id");
        String privateKeyBase64 = env.getProperty("apple.private.key");
        String redirectUri = env.getProperty("apple.redirect.uri");

        if (privateKeyBase64 == null) {
            throw new IllegalStateException("APPLE_PRIVATE_KEY not found in environment");
        }

        // PEM 직접 입력 또는 Base64 인코딩된 PEM 모두 지원
        String privateKeyPem;
        if (privateKeyBase64.contains("-----BEGIN")) {
            privateKeyPem = privateKeyBase64.replace("\\n", "\n");
        } else {
            privateKeyPem = new String(
                    Base64.getDecoder().decode(privateKeyBase64),
                    StandardCharsets.UTF_8
            );
        }

        // client_secret(JWT) 생성
        String clientSecret = AppleJwtUtil.createClientSecret(
                clientId, teamId, keyId, privateKeyPem
        );

        // 네이티브 앱 플로우에서는 redirect_uri 미포함
        String body = "grant_type=authorization_code"
                + "&code=" + authorizationCode
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://appleid.apple.com/auth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = objectMapper.readTree(response.body());
        int statusCode = response.statusCode();
        boolean hasIdToken = json.hasNonNull("id_token");

        if (!hasIdToken) {
            String error = json.hasNonNull("error") ? json.get("error").asText() : "unknown_error";
            String errorDescription = json.hasNonNull("error_description")
                    ? json.get("error_description").asText()
                    : "no_description";
            log.warn("Apple token exchange failed. status={}, error={}, description={}",
                    statusCode, error, errorDescription);
            throw new IllegalStateException(
                    "Apple token response has no id_token. status=" + statusCode + ", error=" + error
            );
        }

        if (log.isDebugEnabled()) {
            log.debug("Apple token exchange success. status={}, hasIdToken={}", statusCode, hasIdToken);
        }

        return json.get("id_token").asText();
    }
}
