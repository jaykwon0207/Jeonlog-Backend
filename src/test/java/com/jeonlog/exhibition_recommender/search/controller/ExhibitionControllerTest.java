package com.jeonlog.exhibition_recommender.search.controller;

import com.jeonlog.exhibition_recommender.common.api.GlobalExceptionHandler;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
import com.jeonlog.exhibition_recommender.search.service.ExhibitionService;
import com.jeonlog.exhibition_recommender.search.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExhibitionControllerTest {

    @Mock
    private ExhibitionService exhibitionService;

    @Mock
    private SearchService searchService;

    @Mock
    private ExhibitionRepository exhibitionRepository;

    @InjectMocks
    private ExhibitionController exhibitionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exhibitionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSearchRank_whenFromIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/exhibitions/search/rank")
                        .param("from", "not-a-datetime")
                        .param("limit", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message", containsString("from는 ISO-8601 LocalDateTime 형식이어야 합니다.")));

        verify(searchService, never()).getTopKeywords(any(), any(), anyInt());
    }

    @Test
    void getSearchRank_whenParamsAreValid_callsServiceWithParsedDateTime() throws Exception {
        when(searchService.getTopKeywords(any(), any(), eq(2)))
                .thenReturn(List.of(KeywordRankDto.builder().keyword("모네").count(3L).build()));

        mockMvc.perform(get("/api/exhibitions/search/rank")
                        .param("from", "2026-04-01T00:00:00")
                        .param("to", "2026-04-30T23:59:59")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].keyword").value("모네"))
                .andExpect(jsonPath("$.data[0].count").value(3));

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(searchService).getTopKeywords(fromCaptor.capture(), toCaptor.capture(), eq(2));
        assertThat(fromCaptor.getValue()).isEqualTo(LocalDateTime.parse("2026-04-01T00:00:00"));
        assertThat(toCaptor.getValue()).isEqualTo(LocalDateTime.parse("2026-04-30T23:59:59"));
    }
}
