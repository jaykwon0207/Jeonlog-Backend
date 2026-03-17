package com.jeonlog.exhibition_recommender.report.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMyNotices_withoutAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/me/notices"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Unauthorized\"}"));
    }

    @Test
    void getAdminReports_withoutAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/reports"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Unauthorized\"}"));
    }
}
