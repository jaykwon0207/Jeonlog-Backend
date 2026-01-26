package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AppleLoginRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.AppleOAuthAttributes;
import com.jeonlog.exhibition_recommender.auth.service.AppleTokenService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AppleAuthController {

    private final AppleTokenService appleTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/apple")
    public ResponseEntity<ApiResponse<?>> appleLogin(
            @RequestBody AppleLoginRequestDto request
    ) {
        try {
            String idToken =
                    appleTokenService.exchangeCodeForIdToken(
                            request.getAuthorizationCode()
                    );

            AppleOAuthAttributes attributes =
                    AppleOAuthAttributes.fromIdToken(idToken);

            String oauthId = attributes.getSub();

            Optional<User> existing =
                    userRepository.findByOauthIdAndOauthProvider(
                            oauthId, OauthProvider.APPLE
                    );

            User user = existing.orElseGet(() ->
                    userRepository.save(attributes.toEntity())
            );

            String accessToken =
                    jwtTokenProvider.createAccessToken(user);
            String refreshToken =
                    jwtTokenProvider.createRefreshToken(user.getOauthId());

            return ResponseEntity.ok(
                    ApiResponse.ok(
                            new AppleLoginResponse(
                                    accessToken,
                                    refreshToken,
                                    existing.isEmpty()
                            )
                    )
            );

        } catch (Exception e) {
            log.error("Apple login failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("APPLE_LOGIN_FAILED", e.getMessage()));
        }
    }

    private record AppleLoginResponse(
            String accessToken,
            String refreshToken,
            boolean newUser
    ) {}
}