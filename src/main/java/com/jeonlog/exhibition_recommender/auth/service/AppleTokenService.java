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

        // Base64 → PEM 복원
        String privateKeyPem = new String(
                Base64.getDecoder().decode(privateKeyBase64),
                StandardCharsets.UTF_8
        );

        // client_secret(JWT) 생성
        String clientSecret = AppleJwtUtil.createClientSecret(
                clientId, teamId, keyId, privateKeyPem
        );

        String body = "grant_type=authorization_code"
                + "&code=" + authorizationCode
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://appleid.apple.com/auth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = objectMapper.readTree(response.body());
        log.error("🔥 Apple token raw response = {}", json);

        if (!json.has("id_token")) {
            throw new IllegalStateException(
                    "Apple token response has no id_token: " + json.toString()
            );
        }


        return json.get("id_token").asText();
    }
}