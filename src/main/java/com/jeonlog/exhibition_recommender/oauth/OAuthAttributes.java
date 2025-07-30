package com.jeonlog.exhibition_recommender.oauth;

import com.jeonlog.exhibition_recommender.domain.user.Gender;
import com.jeonlog.exhibition_recommender.domain.user.OauthProvider;
import com.jeonlog.exhibition_recommender.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final Map<String, Object> rawAttributes;  // ✅ 전체 JSON 저장
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final Gender gender;
    private final Integer birthYear;
    private final OauthProvider oauthProvider;
    private final String oauthId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, Map<String, Object> rawAttributes, String nameAttributeKey,
                           String name, String email, Gender gender, Integer birthYear,
                           OauthProvider oauthProvider, String oauthId) {
        this.attributes = attributes;
        this.rawAttributes = rawAttributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthYear = birthYear;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver(userNameAttributeName, attributes);
        } else if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth 서비스입니다: " + registrationId);
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String genderStr = (String) response.get("gender");
        Gender gender = null;
        if ("M".equalsIgnoreCase(genderStr)) {
            gender = Gender.MALE;
        } else if ("F".equalsIgnoreCase(genderStr)) {
            gender = Gender.FEMALE;
        }

        String birthYearStr = (String) response.get("birthyear");
        Integer birthYear = birthYearStr != null ? Integer.parseInt(birthYearStr) : 0;

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .gender(gender)
                .birthYear(birthYear)
                .oauthProvider(OauthProvider.NAVER)
                .oauthId((String) response.get("id"))
                .attributes(response)
                .rawAttributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .gender(null)
                .birthYear(0)
                .oauthProvider(OauthProvider.GOOGLE)
                .oauthId((String) attributes.get("email"))
                .attributes(attributes)
                .rawAttributes(attributes)           // ✅ 그대로
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .gender(gender)
                .birthYear(birthYear)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
