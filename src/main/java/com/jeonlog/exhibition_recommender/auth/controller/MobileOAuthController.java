package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.dto.GoogleMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.dto.NaverMobileLoginRequest;
import com.jeonlog.exhibition_recommender.auth.exception.NaverProfileException;
import com.jeonlog.exhibition_recommender.auth.service.MobileOAuthProfileService;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import com.jeonlog.exhibition_recommender.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@Slf4j
public class MobileOAuthController {

    private final MobileOAuthProfileService mobileOAuthProfileService;
    private final OAuthLoginSuccessService successService;

    @PostMapping("/google/mobile")
    public ResponseEntity<ApiResponse<?>> googleMobileLogin(
            @RequestBody GoogleMobileLoginRequest request
    ) {
        log.info("[AUTH] mobile_login_start provider=GOOGLE endpoint=/api/auth/google/mobile");
        try {
            if (request == null || !StringUtils.hasText(request.getType()) || !"success".equalsIgnoreCase(request.getType())) {
                log.warn("[AUTH] mobile_login_failed provider=GOOGLE reason=invalid_google_sdk_response");
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
            log.info("[AUTH] mobile_login_success provider=GOOGLE newUser={}", result.newUser());

            return ResponseEntity.ok(ApiResponse.ok(toPayload(result)));
        } catch (Exception e) {
            log.warn("[AUTH] mobile_login_failed provider=GOOGLE reason={}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("GOOGLE_LOGIN_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/naver/mobile")
    public ResponseEntity<ApiResponse<?>> naverMobileLogin(
            @RequestBody NaverMobileLoginRequest request
    ) {
        log.info("[AUTH] mobile_login_start provider=NAVER endpoint=/api/auth/naver/mobile");
        try {
            if (request == null) {
                return badNaverRequest("NAVER_LOGIN_REQUEST_MISSING", false, false);
            }

            if (!Boolean.TRUE.equals(request.getIsSuccess())) {
                boolean hasSuccessResponse = request.getSuccessResponse() != null;
                boolean hasAccessToken = hasSuccessResponse
                        && StringUtils.hasText(request.getSuccessResponse().getAccessToken());
                return badNaverRequest("NAVER_LOGIN_NOT_SUCCESS", hasSuccessResponse, hasAccessToken);
            }

            if (request.getSuccessResponse() == null) {
                return badNaverRequest("NAVER_LOGIN_SUCCESS_RESPONSE_MISSING", false, false);
            }

            String accessToken = request.getSuccessResponse().getAccessToken();
            if (!StringUtils.hasText(accessToken)) {
                return badNaverRequest("NAVER_LOGIN_ACCESS_TOKEN_MISSING", true, false);
            }

            var naver = mobileOAuthProfileService.fetchNaverProfile(accessToken);

            var result = successService.handle(
                    "NAVER",
                    naver.id(),
                    naver.email(),
                    StringUtils.hasText(naver.name()) ? naver.name() : "Naver User",
                    false
            );
            log.info("[AUTH] mobile_login_success provider=NAVER newUser={}", result.newUser());

            return ResponseEntity.ok(ApiResponse.ok(toPayload(result)));
        } catch (NaverProfileException e) {
            HttpStatus status = e.isUpstream() ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
            log.warn(
                    "[AUTH] mobile_login_failed provider=NAVER reasonCode={} status={} hasSuccessResponse={} hasAccessToken={}",
                    e.getCode(),
                    status.value(),
                    request != null && request.getSuccessResponse() != null,
                    request != null
                            && request.getSuccessResponse() != null
                            && StringUtils.hasText(request.getSuccessResponse().getAccessToken())
            );
            return ResponseEntity.status(status)
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            boolean hasSuccessResponse = request != null && request.getSuccessResponse() != null;
            boolean hasAccessToken = hasSuccessResponse
                    && StringUtils.hasText(request.getSuccessResponse().getAccessToken());
            log.warn(
                    "[AUTH] mobile_login_failed provider=NAVER reasonCode=NAVER_LOGIN_FAILED status={} hasSuccessResponse={} hasAccessToken={} reason={}",
                    HttpStatus.BAD_REQUEST.value(),
                    hasSuccessResponse,
                    hasAccessToken,
                    e.getMessage()
            );
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("NAVER_LOGIN_FAILED", e.getMessage()));
        }
    }

    private ResponseEntity<ApiResponse<?>> badNaverRequest(
            String reasonCode,
            boolean hasSuccessResponse,
            boolean hasAccessToken
    ) {
        log.warn(
                "[AUTH] mobile_login_failed provider=NAVER reasonCode={} status={} hasSuccessResponse={} hasAccessToken={}",
                reasonCode,
                HttpStatus.BAD_REQUEST.value(),
                hasSuccessResponse,
                hasAccessToken
        );
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(reasonCode, reasonCode));
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
