package com.jeonlog.exhibition_recommender.oauth.service;

import com.jeonlog.exhibition_recommender.oauth.exception.OAuth2AuthenticationRedirectException;
import com.jeonlog.exhibition_recommender.oauth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

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
            httpSession.setAttribute("user", existingUser);
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("USER")),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey()
            );
        }

        // 신규 사용자 - 성별과 출생연도 입력 필요
        httpSession.setAttribute("tempOAuthAttributes", attributes);
        throw new OAuth2AuthenticationRedirectException("/oauth/add-info");
    }
}
