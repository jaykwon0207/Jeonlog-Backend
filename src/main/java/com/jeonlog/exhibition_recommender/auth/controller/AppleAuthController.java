package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AppleLoginRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.AppleOAuthAttributes;
import com.jeonlog.exhibition_recommender.auth.dto.TempOAuthDto;
import com.jeonlog.exhibition_recommender.auth.service.AppleTokenService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AppleAuthController {

    private final AppleTokenService appleTokenService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @PostMapping("/apple")
    public ResponseEntity<ApiResponse<?>> appleLogin(
            @RequestBody AppleLoginRequestDto request
    ) {
        try {
            // 1️⃣ authorization_code → id_token
            String idToken =
                    appleTokenService.exchangeCodeForIdToken(
                            request.getAuthorizationCode()
                    );

            // 2️⃣ id_token 파싱
            AppleOAuthAttributes attributes =
                    AppleOAuthAttributes.fromIdToken(idToken);

            String email = attributes.getEmail();

            // 3️⃣ 기존 회원 확인
            Optional<User> existingUser =
                    userRepository.findByEmail(email);

            // ✅ 기존 회원 → 바로 로그인
            if (existingUser.isPresent()) {
                User user = existingUser.get();

                return ResponseEntity.ok(
                        ApiResponse.ok(
                                Map.of(
                                        "accessToken",
                                        jwtTokenProvider.createAccessToken(user),
                                        "refreshToken",
                                        jwtTokenProvider.createRefreshToken(user.getEmail()),
                                        "newUser",
                                        false
                                )
                        )
                );
            }

            // ✅ 신규 회원 → Temp-Token 생성
            TempOAuthDto tempDto = TempOAuthDto.builder()
                    .email(attributes.getEmail())
                    .oauthProvider(OauthProvider.APPLE.name())
                    .oauthId(attributes.getSub())
                    .build();


            String json =
                    objectMapper.writeValueAsString(tempDto);

            String base64 =
                    Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    json.getBytes(StandardCharsets.UTF_8)
                            );

            String tempToken =
                    jwtTokenProvider.createTempToken(
                            base64,
                            10 * 60 * 1000 // 10분
                    );

            return ResponseEntity.ok(
                    ApiResponse.ok(
                            Map.of(
                                    "tempToken", tempToken,
                                    "newUser", true
                            )
                    )
            );

        } catch (Exception e) {
            log.error("Apple login failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "APPLE_LOGIN_FAILED",
                            e.getMessage()
                    ));
        }
    }
}