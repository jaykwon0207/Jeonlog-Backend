package com.jeonlog.exhibition_recommender.auth.service;

import com.jeonlog.exhibition_recommender.auth.exception.NaverProfileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileOAuthProfileService {

    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleWebClientId;

    @Value("${google.mobile.allowed-client-ids:}")
    private String googleAllowedClientIds;

    public record GoogleProfile(String sub, String email, String name) {}

    public record NaverProfile(String id, String email, String name) {}

    public GoogleProfile verifyGoogleIdToken(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new IllegalArgumentException("GOOGLE_ID_TOKEN_REQUIRED");
        }

        Map<String, Object> tokenInfo = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("oauth2.googleapis.com")
                        .path("/tokeninfo")
                        .queryParam("id_token", idToken)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (tokenInfo == null) {
            throw new IllegalStateException("GOOGLE_TOKENINFO_EMPTY");
        }

        String sub = asString(tokenInfo.get("sub"));
        String email = asString(tokenInfo.get("email"));
        String name = asString(tokenInfo.get("name"));
        String aud = asString(tokenInfo.get("aud"));
        String azp = asString(tokenInfo.get("azp"));
        String exp = asString(tokenInfo.get("exp"));

        if (!StringUtils.hasText(sub)) {
            throw new IllegalArgumentException("GOOGLE_SUB_MISSING");
        }

        validateGoogleAudience(aud, azp);
        validateExpiry(exp);

        return new GoogleProfile(sub, email, name);
    }

    public NaverProfile fetchNaverProfile(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw NaverProfileException.badRequest("NAVER_ACCESS_TOKEN_REQUIRED", "NAVER_ACCESS_TOKEN_REQUIRED");
        }

        Map<String, Object> raw;
        try {
            raw = webClient.get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException | WebClientRequestException e) {
            throw NaverProfileException.upstream("NAVER_PROFILE_UPSTREAM_ERROR", "NAVER_PROFILE_UPSTREAM_ERROR");
        }

        if (raw == null) {
            throw NaverProfileException.upstream("NAVER_PROFILE_EMPTY", "NAVER_PROFILE_EMPTY");
        }

        String resultCode = asString(raw.get("resultcode"));
        Object responseObj = raw.get("response");
        if (!"00".equals(resultCode) || !(responseObj instanceof Map<?, ?> responseMap)) {
            throw NaverProfileException.upstream("NAVER_PROFILE_FETCH_FAILED", "NAVER_PROFILE_FETCH_FAILED");
        }

        String id = asString(responseMap.get("id"));
        String email = asString(responseMap.get("email"));
        String name = asString(responseMap.get("name"));

        if (!StringUtils.hasText(id)) {
            throw NaverProfileException.upstream("NAVER_ID_MISSING", "NAVER_ID_MISSING");
        }

        return new NaverProfile(id, email, name);
    }

    private void validateGoogleAudience(String aud, String azp) {
        Set<String> allowed = new HashSet<>();
        if (StringUtils.hasText(googleWebClientId)) {
            allowed.add(googleWebClientId);
        }
        if (StringUtils.hasText(googleAllowedClientIds)) {
            allowed.addAll(Arrays.stream(googleAllowedClientIds.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet()));
        }

        if (allowed.isEmpty()) {
            return;
        }

        if (allowed.contains(aud) || allowed.contains(azp)) {
            return;
        }

        throw new IllegalArgumentException("GOOGLE_AUDIENCE_MISMATCH");
    }

    private void validateExpiry(String exp) {
        if (!StringUtils.hasText(exp)) {
            return;
        }

        long expEpoch = Long.parseLong(exp);
        if (Instant.ofEpochSecond(expEpoch).isBefore(Instant.now())) {
            throw new IllegalArgumentException("GOOGLE_ID_TOKEN_EXPIRED");
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
