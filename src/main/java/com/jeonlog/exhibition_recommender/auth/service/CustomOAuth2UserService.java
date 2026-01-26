package com.jeonlog.exhibition_recommender.auth.service;

import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        OAuthAttributes attributes =
                OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        // ✅ 공통 attribute 계약
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("provider", attributes.getOauthProvider().name());
        customAttributes.put("oauthId", attributes.getOauthId());
        customAttributes.put("email", attributes.getEmail());
        customAttributes.put("name", attributes.getName());

        boolean exists =
                userRepository.existsByOauthProviderAndOauthId(
                        attributes.getOauthProvider(),
                        attributes.getOauthId()
                );

        if (exists) {
            return new DefaultOAuth2User(
                    Set.of(new SimpleGrantedAuthority("USER")),
                    customAttributes,
                    "oauthId"
            );
        }

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("NEW_USER")),
                customAttributes,
                "oauthId"
        );
    }
}