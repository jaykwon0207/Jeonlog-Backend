package com.jeonlog.exhibition_recommender.exhibition.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionDetailResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.dto.ExhibitionResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionImageResponseDto;
import com.jeonlog.exhibition_recommender.search.dto.ExhibitionSearchResponseDto;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.search.service.ExhibitionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExhibitionServiceTest {

    @Mock
    private ExhibitionRepository exhibitionRepository;

    @InjectMocks
    private ExhibitionService exhibitionService;

    private Exhibition testExhibition;

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
    }

    @Test
    void getAllExhibitions_ShouldReturnExhibitionList() {
        // Given
        List<Exhibition> exhibitions = Arrays.asList(testExhibition);
        when(exhibitionRepository.findAll()).thenReturn(exhibitions);

        // When
        List<ExhibitionImageResponseDto> result = exhibitionService.getAllExhibitions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exhibitionRepository, times(1)).findAll();
    }

    @Test
    void getExhibitionDetailById_WithValidId_ShouldReturnExhibitionDetail() {
        // Given
        when(exhibitionRepository.findById(1L)).thenReturn(Optional.of(testExhibition));

        // When
        ExhibitionDetailResponseDto result = exhibitionService.getExhibitionDetailById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testExhibition.getTitle(), result.getTitle());
        assertEquals(testExhibition.getDescription(), result.getDescription());
        verify(exhibitionRepository, times(1)).findById(1L);
    }

    @Test
    void getExhibitionDetailById_WithInvalidId_ShouldThrowException() {
        // Given
        when(exhibitionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            exhibitionService.getExhibitionDetailById(999L);
        });
        verify(exhibitionRepository, times(1)).findById(999L);
    }

    @Test
    void searchExhibitions_WithValidQuery_ShouldReturnFilteredResults() {
        // Given
        List<Exhibition> exhibitions = Arrays.asList(testExhibition);
        when(exhibitionRepository.findAll()).thenReturn(exhibitions);

        // When
        List<ExhibitionSearchResponseDto> result = exhibitionService.searchExhibitions("테스트", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExhibition.getTitle(), result.get(0).getTitle());
    }

    @Test
    void searchExhibitions_WithEmptyQuery_ShouldReturnAllResults() {
        // Given
        List<Exhibition> exhibitions = Arrays.asList(testExhibition);
        when(exhibitionRepository.findAll()).thenReturn(exhibitions);

        // When
        List<ExhibitionSearchResponseDto> result = exhibitionService.searchExhibitions("", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
} 