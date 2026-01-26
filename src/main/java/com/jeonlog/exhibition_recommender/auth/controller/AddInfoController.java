package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.TempOAuthDto;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
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
            @RequestHeader("Temp-Token") String tempToken,
            @RequestBody AddInfoRequestDto request
    ) {
        try {
            String base64Data = jwtTokenProvider.getDataFromTempToken(tempToken);
            TempOAuthDto dto = objectMapper.readValue(
                    decodeBase64Url(base64Data), TempOAuthDto.class
            );

            if (userRepository.existsByNickname(request.getNickname())) {
                return ResponseEntity.status(409)
                        .body(ApiResponse.error("DUPLICATE_NICKNAME", "이미 사용 중"));
            }

            User user = userRepository
                    .findByOauthProviderAndOauthId(
                            OauthProvider.valueOf(dto.getOauthProvider()),
                            dto.getOauthId()
                    )
                    .orElseThrow(() -> new IllegalStateException("user not found"));

            user.completeOnboarding(
                    request.getGender(),
                    request.getBirthYear(),
                    request.getNickname()
            );

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtTokenProvider.createAccessToken(user));
            tokens.put("refreshToken", jwtTokenProvider.createRefreshToken(user.getEmail()));

            return ResponseEntity.ok(ApiResponse.ok(tokens));

        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_TEMP_TOKEN", "임시 토큰 오류"));
        }
    }

    private String decodeBase64Url(String input) {
        int pad = input.length() % 4;
        if (pad != 0) input += "====".substring(pad);
        return new String(Base64.getUrlDecoder().decode(input));
    }
}