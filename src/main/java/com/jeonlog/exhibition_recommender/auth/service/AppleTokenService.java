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
        String privateKey = env.getProperty("apple.private.key");
        String redirectUri = env.getProperty("apple.redirect.uri");

        // 1️⃣ Apple용 client_secret 생성
        String clientSecret = AppleJwtUtil.createClientSecret(clientId, teamId, keyId, privateKey);

        // 2️⃣ 토큰 요청
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

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = objectMapper.readTree(response.body());
        log.info("Apple Token Response: {}", json);

        return json.get("id_token").asText();
    }
}