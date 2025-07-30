package com.jeonlog.exhibition_recommender.oauth;

import com.jeonlog.exhibition_recommender.domain.user.User;
import com.jeonlog.exhibition_recommender.domain.user.UserRepository;
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
import java.util.Map;
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

        // Google 사용자의 경우 성별/출생연도 없으면 추가 입력 요청
        if ("google".equals(registrationId)) {
            Optional<User> existingUser = userRepository.findByEmail(attributes.getEmail());
            if (existingUser.isEmpty()) {
                httpSession.setAttribute("tempOAuthAttributes", attributes);
                throw new OAuth2AuthenticationRedirectException("/oauth/add-info");
            }
        }

        // 사용자 저장 또는 업데이트
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", user);

        return new DefaultOAuth2User(
                Collections.singleton(() -> "USER"),
                attributes.getRawAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findByEmail(attributes.getEmail())
                .map(user -> {
                    user.update(attributes.getName());
                    return user;
                })
                .orElseGet(() -> userRepository.save(attributes.toEntity()));
    }
}
