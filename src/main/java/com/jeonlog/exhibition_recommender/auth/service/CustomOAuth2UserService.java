package com.jeonlog.exhibition_recommender.auth.service;

import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession httpSession; // 신규 사용자 처리용

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest
                .getClientRegistration() // 이번 OAuth 로그인의 설정 정보
                .getProviderDetails() // OAuth 제공자(구글/네이버)가 어떻게 작동하는지에 대한 세부정보
                .getUserInfoEndpoint() // 사용자 정보를 어디서, 어떻게 가져오는지 정보 // 사용자의 고유 ID로 쓸 필드 이름
                .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId, // 어떤 provider인지
                userNameAttributeName, // provider가 "고유 ID로 쓰는 필드 이름"
                oAuth2User.getAttributes() // 구글/네이버가 넘겨준 raw JSON을 MAP<String, Object>로 가진것
        );

        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());

        Map<String, Object> customAttributes = new HashMap<>(attributes.getAttributes());
        customAttributes.put("email", attributes.getEmail());
        customAttributes.put("name", attributes.getName());
        customAttributes.put("provider", attributes.getOauthProvider().name());
        customAttributes.put("id", attributes.getOauthId());

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.update(attributes.getName());

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("USER")),
                    customAttributes,
                    attributes.getNameAttributeKey()
            );
        }

        httpSession.setAttribute("tempOAuthAttributes", attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("NEW_USER")),
                customAttributes,
                attributes.getNameAttributeKey()
        );
    }
}