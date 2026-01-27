package com.jeonlog.exhibition_recommender.auth.service;

import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName =
                userRequest.getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        Map<String, Object> customAttributes = new HashMap<>(attributes.getAttributes());
        customAttributes.put("email", attributes.getEmail());
        customAttributes.put("name", attributes.getName());
        customAttributes.put("provider", attributes.getOauthProvider().name());
        customAttributes.put("id", attributes.getOauthId());

        Optional<User> userOptional =
                userRepository.findByOauthProviderAndOauthId(
                        attributes.getOauthProvider(),
                        attributes.getOauthId()
                );

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.update(attributes.getName());

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("USER")),
                    customAttributes,
                    attributes.getNameAttributeKey()
            );
        }

        // 신규 User 생성 (단 1번)
        userRepository.save(
                User.builder()
                        .email(attributes.getEmail()) // Apple null 가능
                        .name(attributes.getName())
                        .oauthProvider(attributes.getOauthProvider())
                        .oauthId(attributes.getOauthId())
                        .nickname(
                                attributes.getOauthProvider().name().toLowerCase()
                                        + "_" + attributes.getOauthId().substring(0, 8)
                        )
                        .build()
        );

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("NEW_USER")),
                customAttributes,
                attributes.getNameAttributeKey()
        );
    }
}