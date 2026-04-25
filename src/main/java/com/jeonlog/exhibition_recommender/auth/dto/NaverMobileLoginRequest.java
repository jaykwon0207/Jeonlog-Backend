package com.jeonlog.exhibition_recommender.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NaverMobileLoginRequest {
    private Boolean isSuccess;
    private SuccessResponse successResponse;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SuccessResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private String expiresAtUnixSecondString;
    }
}
