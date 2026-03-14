package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.auth.dto.AddInfoRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.TempOAuthDto;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@Slf4j
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

            OauthProvider provider = OauthProvider.valueOf(dto.getOauthProvider());

            User user = userRepository
                    .findByOauthProviderAndOauthId(provider, dto.getOauthId())
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .oauthProvider(provider)
                                    .oauthId(dto.getOauthId())
                                    .email(dto.getEmail())
                                    .name(StringUtils.hasText(dto.getName()) ? dto.getName() : "User")
                                    .build()
                    ));

            user.completeOnboarding(
                    request.getGender(),
                    request.getBirthYear(),
                    request.getNickname()
            );

            userRepository.save(user);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtTokenProvider.createAccessToken(user));
            tokens.put("refreshToken", jwtTokenProvider.createRefreshToken(user));

            return ResponseEntity.ok(ApiResponse.ok(tokens));

        } catch (JwtException e) {
            log.warn("[ADD-INFO] invalid temp jwt token: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_TEMP_TOKEN", "임시 토큰이 유효하지 않습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("[ADD-INFO] invalid temp payload/provider: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("INVALID_TEMP_PAYLOAD", "임시 토큰 데이터가 올바르지 않습니다."));
        } catch (Exception e) {
            log.error("[ADD-INFO] failed to complete signup", e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("ADD_INFO_FAILED", "온보딩 저장 중 오류가 발생했습니다."));
        }
    }

    private String decodeBase64Url(String input) {
        int pad = input.length() % 4;
        if (pad != 0) input += "====".substring(pad);
        return new String(Base64.getUrlDecoder().decode(input));
    }
}
