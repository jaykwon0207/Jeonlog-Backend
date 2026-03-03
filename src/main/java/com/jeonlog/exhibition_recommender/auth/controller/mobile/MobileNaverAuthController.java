package com.jeonlog.exhibition_recommender.auth.controller.mobile;

import com.jeonlog.exhibition_recommender.auth.dto.mobile.NaverMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import com.jeonlog.exhibition_recommender.auth.service.mobile.NaverMobileAuthService;
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
public class MobileNaverAuthController {

    private final NaverMobileAuthService naverMobileAuthService;

    @PostMapping("/naver/mobile")
    public ResponseEntity<ApiResponse<?>> naverMobileLogin(
            @Valid @RequestBody NaverMobileLoginRequest request
    ) {
        try {
            OAuthLoginSuccessService.Result result = naverMobileAuthService.loginWithPkce(request);
            return ResponseEntity.ok(ApiResponse.ok(toResponse(result)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NAVER_MOBILE_LOGIN_FAILED", e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(OAuthLoginSuccessService.Result result) {
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
