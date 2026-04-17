package com.jeonlog.exhibition_recommender.notification.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.notification.repository.ServiceAnnouncementRepository;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.Role;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceAnnouncementAdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ServiceAnnouncementRepository serviceAnnouncementRepository;

    @BeforeEach
    void setUp() {
        serviceAnnouncementRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createAnnouncement_withoutAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/admin/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Unauthorized\"}"));
    }

    @Test
    void createAnnouncement_withUserRole_returnsForbiddenAndDoesNotCreateData() throws Exception {
        User user = saveUser(Role.USER, "security-user");
        String accessToken = jwtTokenProvider.createAccessToken(user);
        long beforeCount = serviceAnnouncementRepository.count();

        mockMvc.perform(post("/api/admin/announcements")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        assertThat(serviceAnnouncementRepository.count()).isEqualTo(beforeCount);
    }

    @Test
    void createAnnouncement_withAdminRole_returnsOk() throws Exception {
        User admin = saveUser(Role.ADMIN, "security-admin");
        String accessToken = jwtTokenProvider.createAccessToken(admin);
        long beforeCount = serviceAnnouncementRepository.count();

        mockMvc.perform(post("/api/admin/announcements")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지 발행 완료"))
                .andExpect(jsonPath("$.announcementId").isNumber());

        assertThat(serviceAnnouncementRepository.count()).isEqualTo(beforeCount + 1);
    }

    private User saveUser(Role role, String oauthId) {
        return userRepository.save(User.builder()
                .email(oauthId + "@example.com")
                .name("security-test")
                .oauthProvider(OauthProvider.GOOGLE)
                .oauthId(oauthId)
                .role(role)
                .build());
    }

    private String requestBody() {
        return """
                {
                  "title": "운영 점검 공지",
                  "body": "점검 안내입니다.",
                  "pushEnabled": false,
                  "imageUrls": []
                }
                """;
    }
}
