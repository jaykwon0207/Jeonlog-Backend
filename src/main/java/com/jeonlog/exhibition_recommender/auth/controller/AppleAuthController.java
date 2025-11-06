package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AppleLoginRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.AppleOAuthAttributes;
import com.jeonlog.exhibition_recommender.auth.service.AppleTokenService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
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
    public ResponseEntity<ApiResponse<?>> appleLogin(@RequestBody AppleLoginRequestDto request) {
        try {
            // 1️⃣ Apple 서버에 authorization_code 교환 요청
            String idToken = appleTokenService.exchangeCodeForIdToken(request.getAuthorizationCode());

            // 2️⃣ id_token에서 사용자 정보 파싱
            AppleOAuthAttributes attributes = AppleOAuthAttributes.fromIdToken(idToken);
            String email = attributes.getEmail();
            String oauthId = attributes.getSub();

            // 3️⃣ DB에 사용자 존재 확인 또는 신규 저장
            Optional<User> existing = userRepository.findByEmail(email);
            User user = existing.orElseGet(() ->
                    userRepository.save(attributes.toEntity())
            );

            // 4️⃣ JWT 발급
            String accessToken = jwtTokenProvider.createAccessToken(email);
            String refreshToken = jwtTokenProvider.createRefreshToken(email);

            // 5️⃣ 응답 (신규 유저 여부 포함)
            return ResponseEntity.ok(ApiResponse.ok(
                    new AppleLoginResponse(accessToken, refreshToken, existing.isEmpty())
            ));

        } catch (Exception e) {
            log.error("Apple login failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("APPLE_LOGIN_FAILED", e.getMessage()));
        }
    }

    private record AppleLoginResponse(String accessToken, String refreshToken, boolean newUser) {}
}