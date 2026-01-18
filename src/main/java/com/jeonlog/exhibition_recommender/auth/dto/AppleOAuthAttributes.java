package com.jeonlog.exhibition_recommender.auth.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AppleOAuthAttributes {

    private final String sub;
    private final String email;

    public static AppleOAuthAttributes fromIdToken(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        Map<String, Object> payload =
                new ObjectMapper().readValue(payloadJson, Map.class);

        return new AppleOAuthAttributes(
                (String) payload.get("sub"),
                (String) payload.get("email")
        );
    }

    public User toEntity() {
        return User.builder()
                .oauthId(sub)
                .oauthProvider(OauthProvider.APPLE)
                .email(email)
                .name("Apple User")
                .nickname("apple_" + UUID.randomUUID().toString().substring(0, 8))
                .build();
    }
}