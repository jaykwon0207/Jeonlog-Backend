package com.jeonlog.exhibition_recommender.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.support.MySqlContainerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "review-login.enabled=true",
        "review-login.code=REVIEW-CODE-1234",
        "review-login.oauth-id=test-reviewer",
        "review-login.name=Test Reviewer"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuthControllerIntegrationTest extends MySqlContainerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reviewerLogin_withInvalidCode_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/reviewer-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewCode\":\"WRONG-CODE\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REVIEW_CODE"));
    }

    @Test
    void reviewerLogin_withValidCode_returnsAccessAndRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/auth/reviewer-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewCode\":\"REVIEW-CODE-1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.reviewer").value(true));
    }

    @Test
    void issueAccessToken_withoutRefreshToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NO_REFRESH"));
    }

    @Test
    void issueAccessToken_withValidRefreshToken_returnsNewAccessToken() throws Exception {
        String refreshToken = issueReviewerRefreshToken();

        mockMvc.perform(post("/api/auth/access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isString());
    }

    private String issueReviewerRefreshToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/reviewer-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewCode\":\"REVIEW-CODE-1234\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        JsonNode tokenNode = root.path("data").path("refreshToken");
        assertThat(tokenNode.isTextual()).isTrue();
        assertThat(tokenNode.asText()).isNotBlank();
        return tokenNode.asText();
    }
}
