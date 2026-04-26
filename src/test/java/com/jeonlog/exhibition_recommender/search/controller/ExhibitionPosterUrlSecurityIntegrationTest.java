package com.jeonlog.exhibition_recommender.search.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.domain.VenueType;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenueRepository;
import com.jeonlog.exhibition_recommender.support.MySqlContainerTestSupport;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.Role;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ExhibitionPosterUrlSecurityIntegrationTest extends MySqlContainerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private ExhibitionRepository exhibitionRepository;

    private User adminUser;
    private User normalUser;
    private Exhibition exhibition;

    @BeforeEach
    void setUp() {
        exhibitionRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = userRepository.save(buildUser(Role.ADMIN, "admin"));
        normalUser = userRepository.save(buildUser(Role.USER, "user"));

        Venue venue = venueRepository.save(Venue.builder()
                .name("테스트 전시장")
                .type(VenueType.GALLERY)
                .build());

        exhibition = exhibitionRepository.save(Exhibition.builder()
                .title("테스트 전시")
                .description("설명")
                .location("서울")
                .posterUrl("https://example.com/old-poster.jpg")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .price(10000)
                .isFree(false)
                .exhibitionTheme(ExhibitionTheme.CASUAL_VISIT)
                .genre(GenreType.MODERN_ART)
                .contact("02-0000-0000")
                .website("https://example.com")
                .viewingTime("10:00-18:00")
                .personalizedPosterUrl("https://example.com/personalized.jpg")
                .generalRecommendationsPosterUrl("https://example.com/general.jpg")
                .venue(venue)
                .build());
    }

    @Test
    void updatePosterUrl_withoutAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/exhibitions/{id}/poster-url", exhibition.getId())
                        .param("posterUrl", "https://example.com/new-poster.jpg"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Unauthorized\"}"));
    }

    @Test
    void updatePosterUrl_withUserToken_returnsForbidden() throws Exception {
        String userToken = jwtTokenProvider.createAccessToken(normalUser);

        mockMvc.perform(post("/api/exhibitions/{id}/poster-url", exhibition.getId())
                        .param("posterUrl", "https://example.com/new-poster.jpg")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void updatePosterUrl_withAdminToken_updatesPosterUrl() throws Exception {
        String adminToken = jwtTokenProvider.createAccessToken(adminUser);
        String newPosterUrl = "https://example.com/new-poster.jpg";

        mockMvc.perform(post("/api/exhibitions/{id}/poster-url", exhibition.getId())
                        .param("posterUrl", newPosterUrl)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Exhibition updated = exhibitionRepository.findById(exhibition.getId()).orElseThrow();
        assertThat(updated.getPosterUrl()).isEqualTo(newPosterUrl);
    }

    private User buildUser(Role role, String prefix) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return User.builder()
                .email(prefix + "-" + unique + "@example.com")
                .name(prefix + "-name-" + unique)
                .oauthProvider(OauthProvider.GOOGLE)
                .oauthId(prefix + "-oauth-" + unique)
                .nickname(prefix + "-nick-" + unique)
                .role(role)
                .build();
    }
}
