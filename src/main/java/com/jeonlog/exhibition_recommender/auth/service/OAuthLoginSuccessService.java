package com.jeonlog.exhibition_recommender.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthLoginSuccessService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public record Result(
            boolean newUser,
            String accessToken,
            String refreshToken,
            String tempToken
    ) {}

    public Result handle(
            String providerStr,
            String oauthId,
            String email,
            String name,
            boolean ignored   // ❌ 더 이상 신뢰하지 않음
    ) throws Exception {

        OauthProvider provider = OauthProvider.valueOf(providerStr);

        Optional<User> userOpt =
                userRepository.findByOauthProviderAndOauthId(provider, oauthId);

        boolean isNewUser = userOpt.isEmpty();

        User user = userOpt.orElseGet(() ->
                userRepository.save(
                        User.builder()
                                .oauthProvider(provider)
                                .oauthId(oauthId)
                                .email(email)
                                .name(name)
                                .build()
                )
        );

        if (isNewUser) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("oauthProvider", provider.name());
            temp.put("oauthId", oauthId);
            temp.put("email", email);
            temp.put("name", name);

            String json = objectMapper.writeValueAsString(temp);
            String base64 = Base64.getUrlEncoder().encodeToString(json.getBytes());

            String tempToken =
                    jwtTokenProvider.createTempToken(base64, 60 * 60 * 1000);

            return new Result(true, null, null, tempToken);
        }

        return new Result(
                false,
                jwtTokenProvider.createAccessToken(user),
                jwtTokenProvider.createRefreshToken(user),
                null
        );
    }
}