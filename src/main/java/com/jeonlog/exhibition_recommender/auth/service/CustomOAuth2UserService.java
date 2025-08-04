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
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession httpSession; // ❗신규 사용자 처리용으로만 유지

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

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.update(attributes.getName());
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("USER")),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey()
            );
        }

        // ✅ 신규 사용자: 세션에 임시 정보 저장
        httpSession.setAttribute("tempOAuthAttributes", attributes);

        // ✅ 강제로 OAuth2 인증 실패 예외 발생 (Spring Security의 실패 핸들러로 이동)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("NEW_USER")),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }
}