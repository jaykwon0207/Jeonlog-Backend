package com.jeonlog.exhibition_recommender.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
            boolean ignored
    ) throws Exception {

        OauthProvider provider = OauthProvider.valueOf(providerStr);
        Optional<User> existing = userRepository.findByOauthProviderAndOauthId(provider, oauthId);

        if (existing.isPresent()) {
            User user = existing.get();
            log.info("[AUTH] login_success provider={} userId={} newUser=false", provider, user.getId());
            return new Result(
                    false,
                    jwtTokenProvider.createAccessToken(user),
                    jwtTokenProvider.createRefreshToken(user),
                    null
            );
        }

        Map<String, Object> temp = new HashMap<>();
        temp.put("oauthProvider", provider.name());
        temp.put("oauthId", oauthId);
        temp.put("email", email);
        temp.put("name", name);

        String json = objectMapper.writeValueAsString(temp);
        String base64 = Base64.getUrlEncoder().encodeToString(json.getBytes());
        String tempToken = jwtTokenProvider.createTempToken(base64, 60 * 60 * 1000);
        log.info("[AUTH] temp_token_issued provider={} newUser=true", provider);

        return new Result(true, null, null, tempToken);
    }
}
