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
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());

        // 🔽 email 등 필요한 값만 뽑아서 attributes에 직접 넣음 (구글/네이버 모두 통일)
        Map<String, Object> customAttributes = new HashMap<>(attributes.getAttributes());
        customAttributes.put("email", attributes.getEmail());  // ✅ 반드시 email 포함시킴
        customAttributes.put("name", attributes.getName());    // ✅ 이름도 넣어두면 편함

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.update(attributes.getName());

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("USER")),
                    customAttributes,
                    attributes.getNameAttributeKey()
            );
        }

        // ✅ 신규 사용자: 세션에 임시 정보 저장
        httpSession.setAttribute("tempOAuthAttributes", attributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("NEW_USER")),
                customAttributes,
                attributes.getNameAttributeKey()
        );
    }
}