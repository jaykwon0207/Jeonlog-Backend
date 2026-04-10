package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.service.AppleTokenService;
import com.jeonlog.exhibition_recommender.auth.service.MobileOAuthProfileService;
import com.jeonlog.exhibition_recommender.auth.service.OAuthLoginSuccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OAuthErrorResponseSanitizationTest {

    @Mock
    private MobileOAuthProfileService mobileOAuthProfileService;
    @Mock
    private OAuthLoginSuccessService successService;
    @Mock
    private AppleTokenService appleTokenService;

    private MockMvc mobileMockMvc;
    private MockMvc appleMockMvc;

    @BeforeEach
    void setUp() {
        mobileMockMvc = MockMvcBuilders.standaloneSetup(
                new MobileOAuthController(mobileOAuthProfileService, successService)
        ).build();
        appleMockMvc = MockMvcBuilders.standaloneSetup(
                new AppleAuthController(appleTokenService, successService)
        ).build();
    }

    @Test
    void googleMobileLogin_whenError_returnsSanitizedMessage() throws Exception {
        when(mobileOAuthProfileService.verifyGoogleIdToken(anyString()))
                .thenThrow(new IllegalStateException("400 Bad Request from GET https://oauth2.googleapis.com/tokeninfo?id_token=abc"));

        String request = """
                {
                  "type": "success",
                  "data": {
                    "idToken": "dummy-token"
                  }
                }
                """;

        mobileMockMvc.perform(post("/api/auth/google/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GOOGLE_LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("구글 로그인 처리에 실패했습니다."))
                .andExpect(jsonPath("$.message", not(containsString("oauth2.googleapis.com"))))
                .andExpect(jsonPath("$.message", not(containsString("400 Bad Request"))));
    }

    @Test
    void naverMobileLogin_whenError_returnsSanitizedMessage() throws Exception {
        when(mobileOAuthProfileService.fetchNaverProfile(anyString()))
                .thenThrow(new IllegalArgumentException("401 Unauthorized from GET https://openapi.naver.com/v1/nid/me"));

        String request = """
                {
                  "isSuccess": true,
                  "successResponse": {
                    "accessToken": "dummy-access-token"
                  }
                }
                """;

        mobileMockMvc.perform(post("/api/auth/naver/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NAVER_LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("네이버 로그인 처리에 실패했습니다."))
                .andExpect(jsonPath("$.message", not(containsString("openapi.naver.com"))))
                .andExpect(jsonPath("$.message", not(containsString("Unauthorized"))));
    }

    @Test
    void appleLogin_whenError_returnsSanitizedMessage() throws Exception {
        when(appleTokenService.exchangeCodeForIdToken(anyString()))
                .thenThrow(new IllegalStateException("Apple token response has no id_token. status=400, error=invalid_client"));

        String request = """
                {
                  "authorizationCode": "dummy-code"
                }
                """;

        appleMockMvc.perform(post("/api/auth/apple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("APPLE_LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("애플 로그인 처리에 실패했습니다."))
                .andExpect(jsonPath("$.message", not(containsString("id_token"))))
                .andExpect(jsonPath("$.message", not(containsString("status=400"))));
    }
}
