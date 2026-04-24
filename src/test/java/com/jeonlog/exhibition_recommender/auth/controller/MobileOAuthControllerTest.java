package com.jeonlog.exhibition_recommender.auth.controller;

import com.jeonlog.exhibition_recommender.auth.exception.NaverProfileException;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MobileOAuthControllerTest {

    @Mock
    private MobileOAuthProfileService mobileOAuthProfileService;

    @Mock
    private OAuthLoginSuccessService successService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MobileOAuthController controller = new MobileOAuthController(mobileOAuthProfileService, successService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void naverMobileLogin_success_returnsTokens() throws Exception {
        when(mobileOAuthProfileService.fetchNaverProfile("naver-access-token"))
                .thenReturn(new MobileOAuthProfileService.NaverProfile("naver-id", "n@e.com", "Naver User"));
        when(successService.handle(eq("NAVER"), eq("naver-id"), eq("n@e.com"), eq("Naver User"), eq(false)))
                .thenReturn(new OAuthLoginSuccessService.Result(false, "access-token", "refresh-token", null));

        mockMvc.perform(post("/api/auth/naver/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isSuccess": true,
                                  "successResponse": {
                                    "accessToken": "naver-access-token"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.newUser").value(false));
    }

    @Test
    void naverMobileLogin_isSuccessFalse_returnsDetailedCode() throws Exception {
        mockMvc.perform(post("/api/auth/naver/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isSuccess": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NAVER_LOGIN_NOT_SUCCESS"));
    }

    @Test
    void naverMobileLogin_successResponseMissing_returnsDetailedCode() throws Exception {
        mockMvc.perform(post("/api/auth/naver/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isSuccess": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NAVER_LOGIN_SUCCESS_RESPONSE_MISSING"));
    }

    @Test
    void naverMobileLogin_accessTokenMissing_returnsDetailedCode() throws Exception {
        mockMvc.perform(post("/api/auth/naver/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isSuccess": true,
                                  "successResponse": {
                                    "accessToken": ""
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NAVER_LOGIN_ACCESS_TOKEN_MISSING"));
    }

    @Test
    void naverMobileLogin_upstreamFailure_returnsBadGateway() throws Exception {
        when(mobileOAuthProfileService.fetchNaverProfile("naver-access-token"))
                .thenThrow(NaverProfileException.upstream("NAVER_PROFILE_FETCH_FAILED", "NAVER_PROFILE_FETCH_FAILED"));

        mockMvc.perform(post("/api/auth/naver/mobile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isSuccess": true,
                                  "successResponse": {
                                    "accessToken": "naver-access-token"
                                  }
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NAVER_PROFILE_FETCH_FAILED"));
    }
}
