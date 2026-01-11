package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.search.controller.ExhibitionController;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.search.service.ExhibitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExhibitionController.class)
@WithMockUser
class ExhibitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExhibitionService exhibitionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Exhibition testExhibition;
    private ExhibitionResponseDto testResponseDto;
    private ExhibitionDetailResponseDto testDetailResponseDto;
    private ExhibitionSearchResponseDto testSearchResponseDto;
    private ExhibitionResponseDto testImageResponseDto;

    @BeforeEach
    void setUp() {
        testExhibition = Exhibition.builder()
                .id(1L)
                .title("테스트 전시회")
                .description("테스트 설명")
                .location("서울")
                .posterUrl("http://example.com/poster.jpg")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(10000)
                .isFree(false)
                .build();

        testResponseDto = ExhibitionResponseDto.builder()
                .id(1L)
                .title("테스트 전시회")
                .location("서울")
                .posterUrl("http://example.com/poster.jpg")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(10000)
                .build();

        testDetailResponseDto = ExhibitionDetailResponseDto.builder()
                .id(1L)
                .title("테스트 전시회")
                .description("테스트 설명")
                .location("서울")
                .posterUrl("http://example.com/poster.jpg")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .price(10000)
                .isFree(false)
                .build();

        testSearchResponseDto = ExhibitionSearchResponseDto.builder()
                .id(1L)
                .title("테스트 전시회")
                .artist("테스트 작가")
                .location("서울")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .posterUrl("http://example.com/poster.jpg")
                .price(10000)
                .build();

        testImageResponseDto = ExhibitionResponseDto.builder()
                .id(1L)
                .posterUrl("http://example.com/poster.jpg")
                .build();
    }

    @Test
    @WithMockUser
    void getAllExhibitions_ShouldReturnExhibitionList() throws Exception {
        // Given
        List<ExhibitionResponseDto> exhibitions = Arrays.asList(testImageResponseDto);
        when(exhibitionService.getAllExhibitions()).thenReturn(exhibitions);

        // When & Then
        mockMvc.perform(get("/api/exhibitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("테스트 전시회"))
                .andExpect(jsonPath("$[0].location").value("서울"));
    }

    @Test
    @WithMockUser
    void getExhibitionById_WithValidId_ShouldReturnExhibitionDetail() throws Exception {
        // Given
        when(exhibitionService.getExhibitionDetailById(1L)).thenReturn(testDetailResponseDto);

        // When & Then
        mockMvc.perform(get("/api/exhibitions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("테스트 전시회"))
                .andExpect(jsonPath("$.description").value("테스트 설명"));
    }

    @Test
    @WithMockUser
    void searchExhibitions_WithValidQuery_ShouldReturnSearchResults() throws Exception {
        // Given
        List<ExhibitionSearchResponseDto> searchResults = Arrays.asList(testSearchResponseDto);
        when(exhibitionService.searchExhibitions(eq("테스트"), any())).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/exhibitions/search")
                        .param("query", "테스트")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("테스트 전시회"));
    }
} 