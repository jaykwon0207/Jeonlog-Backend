package com.jeonlog.exhibition_recommender.auth.controller.mobile;

import com.jeonlog.exhibition_recommender.auth.dto.mobile.GoogleMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.service.mobile.GoogleMobileAuthService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MobileGoogleAuthController {

    private final GoogleMobileAuthService googleMobileAuthService;

    @PostMapping("/google/mobile")
    public ResponseEntity<ApiResponse<?>> googleMobileLogin(
            @Valid @RequestBody GoogleMobileLoginRequest request
    ) {
        try {
            var result = googleMobileAuthService.loginWithPkce(request);
            return ResponseEntity.ok(ApiResponse.ok(toResponse(result)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("GOOGLE_MOBILE_LOGIN_FAILED", e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService.Result result) {
        if (result.newUser()) {
            return Map.of("token", result.tempToken(), "newUser", true);
        }
        return Map.of(
                "accessToken", result.accessToken(),
                "refreshToken", result.refreshToken(),
                "newUser", false
        );
    }
}
