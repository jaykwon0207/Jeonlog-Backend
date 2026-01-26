package com.jeonlog.exhibition_recommender.auth.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Base64;
import java.util.Map;

@Getter
@AllArgsConstructor
public class AppleOAuthAttributes {

    private final String sub;
    private final String email;

    public static AppleOAuthAttributes fromIdToken(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        String payloadJson =
                new String(Base64.getUrlDecoder().decode(parts[1]));

        Map<String, Object> payload =
                new ObjectMapper().readValue(payloadJson, Map.class);

        return new AppleOAuthAttributes(
                (String) payload.get("sub"),
                (String) payload.get("email")
        );
    }

    // ✅ 핵심: 공통 OAuthAttributes로 변환
    public OAuthAttributes toOAuthAttributes() {
        return OAuthAttributes.builder()
                .oauthProvider(OauthProvider.APPLE)
                .oauthId(sub)
                .email(email)
                .name("Apple User")
                .build();
    }
}