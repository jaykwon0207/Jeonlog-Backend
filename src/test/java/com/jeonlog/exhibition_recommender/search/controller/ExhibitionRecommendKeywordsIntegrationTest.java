package com.jeonlog.exhibition_recommender.search.controller;

import com.jeonlog.exhibition_recommender.auth.config.JwtTokenProvider;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.domain.VenueType;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenueRepository;
import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.search.repository.SearchRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ExhibitionRecommendKeywordsIntegrationTest extends MySqlContainerTestSupport {

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
    @Autowired
    private SearchRepository searchRepository;

    private User user;

    @BeforeEach
    void setUp() {
        searchRepository.deleteAll();
        exhibitionRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(buildUser("tester"));

        Venue venue = venueRepository.save(Venue.builder()
                .name("테스트 전시장")
                .type(VenueType.GALLERY)
                .build());

        Exhibition exhibition = exhibitionRepository.save(Exhibition.builder()
                .title("테스트전시")
                .description("설명")
                .location("서울")
                .posterUrl("https://example.com/poster.jpg")
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

        searchRepository.save(Search.builder()
                .user(user)
                .keyword("테스트전시")
                .searchedAt(LocalDateTime.now().minusDays(1))
                .exhibition(exhibition)
                .build());
    }

    @Test
    void recommendKeywords_withoutAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/exhibitions/search/recommend-keywords"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\":\"Unauthorized\"}"));
    }

    @Test
    void recommendKeywords_withAuthorization_returnsKeywordDtos() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(user);

        mockMvc.perform(get("/api/exhibitions/search/recommend-keywords")
                        .param("limit", "8")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].keyword").isNotEmpty())
                .andExpect(jsonPath("$.data[0].type").isNotEmpty())
                .andExpect(jsonPath("$.data[0].score").isNumber());
    }

    private User buildUser(String prefix) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User built = User.builder()
                .email(prefix + "-" + unique + "@example.com")
                .name(prefix + "-name-" + unique)
                .oauthProvider(OauthProvider.GOOGLE)
                .oauthId(prefix + "-oauth-" + unique)
                .nickname(prefix + "-nick-" + unique)
                .role(Role.USER)
                .build();

        assertThat(built.getRole()).isEqualTo(Role.USER);
        return built;
    }
}
