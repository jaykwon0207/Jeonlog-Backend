package com.jeonlog.exhibition_recommender.auth.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Base64;
import java.util.Map;

@Getter
@AllArgsConstructor
public class AppleOAuthAttributes {

    private final String sub;    // Apple user unique id
    private final String email;  // email (최초 로그인 시만)

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
}