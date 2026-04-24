package com.jeonlog.exhibition_recommender.auth.controller;


import com.jeonlog.exhibition_recommender.auth.dto.AppleLoginRequestDto;
import com.jeonlog.exhibition_recommender.auth.dto.AppleOAuthAttributes;
import com.jeonlog.exhibition_recommender.auth.service.AppleTokenService;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import com.jeonlog.exhibition_recommender.common.logging.TraceIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AppleAuthController {

    private static final String APPLE_LOGIN_FAILED_CODE = "APPLE_LOGIN_FAILED";
    private static final String APPLE_LOGIN_FAILED_MESSAGE = "애플 로그인 처리에 실패했습니다.";

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
            log.warn(
                    "[AUTH] apple_login_failed code={} traceId={} reason={}",
                    APPLE_LOGIN_FAILED_CODE,
                    traceId(),
                    e.getMessage()
            );
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(APPLE_LOGIN_FAILED_CODE, APPLE_LOGIN_FAILED_MESSAGE));
        }
    }

    private String traceId() {
        return MDC.get(TraceIdFilter.TRACE_ID_KEY);
    }
}
