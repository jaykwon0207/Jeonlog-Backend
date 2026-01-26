package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AppleLoginRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.AppleOAuthAttributes;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.auth.dto.TempOAuthDto;
import com.jeonlog.exhibition_recommender.auth.service.AppleTokenService;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AppleAuthController {

    private final AppleTokenService appleTokenService;
    private final OAuthLoginSuccessService successService;

    @PostMapping("/apple")
    public ResponseEntity<ApiResponse<?>> appleLogin(
            @RequestBody AppleLoginRequestDto request
    ) {
        try {
            var attr = AppleOAuthAttributes.fromIdToken(
                    appleTokenService.exchangeCodeForIdToken(
                            request.getAuthorizationCode()
                    )
            );

            var result = successService.handle(
                    "APPLE",
                    attr.getSub(),
                    attr.getEmail(),
                    "Apple User",
                    false   // 신규 여부는 service에서 판단 가능하게 바꿔도 됨
            );

            return ResponseEntity.ok(
                    ApiResponse.ok(
                            result.newUser()
                                    ? Map.of("token", result.tempToken(), "newUser", true)
                                    : Map.of(
                                    "accessToken", result.accessToken(),
                                    "refreshToken", result.refreshToken(),
                                    "newUser", false
                            )
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("APPLE_LOGIN_FAILED", e.getMessage()));
        }
    }
}