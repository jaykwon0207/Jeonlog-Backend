package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.dto.GoogleMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.dto.NaverMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.service.MobileOAuthProfileService;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MobileOAuthController {

    private final MobileOAuthProfileService mobileOAuthProfileService;
    private final OAuthLoginSuccessService successService;

    @PostMapping("/google/mobile")
    public ResponseEntity<ApiResponse<?>> googleMobileLogin(
            @RequestBody GoogleMobileLoginRequest request
    ) {
        try {
            if (request == null || !StringUtils.hasText(request.getType()) || !"success".equalsIgnoreCase(request.getType())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("GOOGLE_LOGIN_FAILED", "invalid google sdk response"));
            }

            String idToken = request != null && request.getData() != null
                    ? request.getData().getIdToken()
                    : null;

            var google = mobileOAuthProfileService.verifyGoogleIdToken(idToken);

            var result = successService.handle(
                    "GOOGLE",
                    google.sub(),
                    google.email(),
                    StringUtils.hasText(google.name()) ? google.name() : "Google User",
                    false
            );

            return ResponseEntity.ok(ApiResponse.ok(toPayload(result)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("GOOGLE_LOGIN_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/naver/mobile")
    public ResponseEntity<ApiResponse<?>> naverMobileLogin(
            @RequestBody NaverMobileLoginRequest request
    ) {
        try {
            if (request == null || !Boolean.TRUE.equals(request.getIsSuccess())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("NAVER_LOGIN_FAILED", "invalid naver sdk response"));
            }

            String accessToken = request != null && request.getSuccessResponse() != null
                    ? request.getSuccessResponse().getAccessToken()
                    : null;

            var naver = mobileOAuthProfileService.fetchNaverProfile(accessToken);

            var result = successService.handle(
                    "NAVER",
                    naver.id(),
                    naver.email(),
                    StringUtils.hasText(naver.name()) ? naver.name() : "Naver User",
                    false
            );

            return ResponseEntity.ok(ApiResponse.ok(toPayload(result)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NAVER_LOGIN_FAILED", e.getMessage()));
        }
    }

    private Map<String, Object> toPayload(OAuthLoginSuccessService.Result result) {
        if (result.newUser()) {
            return Map.of(
                    "token", result.tempToken(),
                    "newUser", true
            );
        }

        return Map.of(
                "accessToken", result.accessToken(),
                "refreshToken", result.refreshToken(),
                "newUser", false
        );
    }
}
