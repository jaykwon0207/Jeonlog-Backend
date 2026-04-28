package com.jeonlog.exhibition_recommender.exhibition.controller;

import com.jeonlog.exhibition_recommender.common.api.GlobalExceptionHandler;
import com.jeonlog.exhibition_recommender.exhibition.dto.VenueListResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VenueControllerTest {

    @Mock
    private VenueService venueService;

    @InjectMocks
    private VenueController venueController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(venueController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void searchVenues_whenQueryIsValid_returnsOkAndDelegatesPageable() throws Exception {
        Page<VenueListResponseDto> page = new PageImpl<>(
                List.of(
                        VenueListResponseDto.builder()
                                .id(1L)
                                .name("서울시립미술관")
                                .address("서울 중구")
                                .build()
                ),
                PageRequest.of(1, 5),
                1
        );
        when(venueService.searchVenues(eq("서울"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/venues/search")
                        .param("query", "서울")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("서울시립미술관"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(venueService).searchVenues(eq("서울"), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void searchVenues_whenQueryIsBlank_returnsBadRequest() throws Exception {
        when(venueService.searchVenues(eq("   "), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("검색어를 입력해주세요."));

        mockMvc.perform(get("/api/venues/search")
                        .param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));

        verify(venueService).searchVenues(eq("   "), any(Pageable.class));
    }
}
