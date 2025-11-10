package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.OAuthAttributes;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import com.jeonlog.exhibition_recommender.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class AddInfoController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> completeSignUp(
            @RequestHeader("Temp-Token") String tempToken,
            @RequestBody AddInfoRequestDto request
    ) {
        try {
            // 1️⃣ tempToken 복호화 → OAuthAttributes 복원
            String base64Data = jwtTokenProvider.getDataFromTempToken(tempToken);
            String json = new String(Base64.getUrlDecoder().decode(base64Data));
            OAuthAttributes attributes = objectMapper.readValue(json, OAuthAttributes.class);

            // 2️⃣ 닉네임 검증
            if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_NICKNAME", "닉네임은 필수 항목입니다."));
            }
            if (userRepository.existsByNickname(request.getNickname())) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."));
            }

            // 3️⃣ 신규 사용자 저장
            User user = userService.saveNewUser(attributes, request);

            // 4️⃣ JWT 발급
            String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

            // 5️⃣ JSON 응답
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);

            return ResponseEntity.ok(ApiResponse.ok(tokens));

        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_TEMP_TOKEN", "임시 토큰이 유효하지 않거나 만료되었습니다."));
        }
    }
}