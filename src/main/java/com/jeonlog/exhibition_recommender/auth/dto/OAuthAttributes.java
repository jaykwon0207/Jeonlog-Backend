package com.jeonlog.exhibition_recommender.auth.dto;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private final String name;
    private final String email;
    private final OauthProvider oauthProvider;
    private final String oauthId;

    public static OAuthAttributes of(
            String registrationId,
            Map<String, Object> attributes
    ) {
        return switch (registrationId) {
            case "google" -> ofGoogle(attributes);
            case "naver" -> ofNaver(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth: " + registrationId);
        };
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .oauthProvider(OauthProvider.GOOGLE)
                .oauthId((String) attributes.get("email"))
                .build();
    }

    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {

        Object responseObj = attributes.get("response");

        Map<String, Object> response =
                responseObj instanceof Map
                        ? (Map<String, Object>) responseObj
                        : attributes; // ⭐ 핵심

        String id = (String) response.get("id");
        String email = (String) response.get("email");
        String name = (String) response.get("name");

        if (id == null) {
            throw new IllegalArgumentException("NAVER oauthId(id) is null");
        }

        return OAuthAttributes.builder()
                .name(name != null ? name : "Naver User")
                .email(email)              // email은 null 허용 가능
                .oauthProvider(OauthProvider.NAVER)
                .oauthId(id)               // ✅ 무조건 id
                .build();
    }
}