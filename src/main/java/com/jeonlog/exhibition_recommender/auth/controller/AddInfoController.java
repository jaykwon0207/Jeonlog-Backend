package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.TempOAuthDto;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class AddInfoController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @PostMapping("/add-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> completeSignUp(
            @RequestHeader(value = "Temp-Token", required = false) String tempToken,
            @RequestBody AddInfoRequestDto request
    ) {
        try {
            if (tempToken == null || tempToken.isBlank()) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("NO_TEMP_TOKEN", "Temp-Token 헤더가 누락되었습니다."));
            }

            String base64Data = jwtTokenProvider.getDataFromTempToken(tempToken);
            String json = decodeBase64Url(base64Data);
            TempOAuthDto dto = objectMapper.readValue(json, TempOAuthDto.class);

            if (dto.getOauthProvider() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_TEMP_TOKEN", "provider 정보가 없습니다."));
            }

            if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("INVALID_NICKNAME", "닉네임은 필수 항목입니다."));
            }
            if (userRepository.existsByNickname(request.getNickname())) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."));
            }

            User user = User.builder()
                    .email(dto.getEmail())
                    .name(dto.getName())
                    .oauthProvider(OauthProvider.valueOf(dto.getOauthProvider()))
                    .oauthId(dto.getOauthId())
                    .gender(request.getGender())
                    .birthYear(request.getBirthYear())
                    .nickname(request.getNickname())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            userRepository.save(user);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtTokenProvider.createAccessToken(dto.getEmail()));
            tokens.put("refreshToken", jwtTokenProvider.createRefreshToken(dto.getEmail()));

            return ResponseEntity.ok(ApiResponse.ok(tokens));

        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_TEMP_TOKEN", "임시 토큰이 유효하지 않거나 만료되었습니다."));
        }
    }

    private String decodeBase64Url(String input) {
        int pad = input.length() % 4;
        if (pad != 0) input += "====".substring(pad);
        return new String(Base64.getUrlDecoder().decode(input));
    }
}