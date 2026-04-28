package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private UserGenreRepository userGenreRepository;
    @Mock
    private ExhibitionRepository exhibitionRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(userGenreRepository, exhibitionRepository);
        when(userGenreRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(UserGenre.builder().userId(USER_ID).build()));
    }

    @Test
    void recommend_returnsTenWhenCandidatesAreEnough() {
        when(exhibitionRepository.findActiveExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(exhibitions(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        List<Exhibition> result = recommendationService.recommend(USER_ID);

        assertThat(result).hasSize(10);
        assertThat(ids(result)).hasSize(10);
    }

    @Test
    void recommend_fillsToAtLeastSevenWhenTargetTenCannotBeMet() {
        when(exhibitionRepository.findActiveExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(exhibitionRepository.findUpcomingExcluding(any(LocalDate.class), any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(exhibitionRepository.findAnyOpenExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(exhibitionRepository.findAnyExcluding(any(), any(Pageable.class)))
                .thenReturn(exhibitions(1, 2, 3, 4, 5, 6))
                .thenReturn(exhibitions(7, 8));

        List<Exhibition> result = recommendationService.recommend(USER_ID);

        assertThat(result.size()).isGreaterThanOrEqualTo(7);
        assertThat(result.size()).isLessThanOrEqualTo(10);
        assertThat(ids(result)).hasSize(result.size());
    }

    @Test
    void recommend_returnsAvailableCountWhenDbHasLessThanSevenUniqueCandidates() {
        when(exhibitionRepository.findActiveExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(exhibitionRepository.findUpcomingExcluding(any(LocalDate.class), any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(exhibitionRepository.findAnyOpenExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(List.of());
        when(exhibitionRepository.findAnyExcluding(any(), any(Pageable.class)))
                .thenReturn(exhibitions(1, 2, 3, 4, 5, 6));

        List<Exhibition> result = recommendationService.recommend(USER_ID);

        assertThat(result).hasSize(6);
        assertThat(ids(result)).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @Test
    void recommend_keepsUniqueExhibitionsAcrossFallbackStages() {
        when(exhibitionRepository.findActiveExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(exhibitions(1, 2, 3));
        when(exhibitionRepository.findUpcomingExcluding(any(LocalDate.class), any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(exhibitions(2, 3, 4));
        when(exhibitionRepository.findAnyOpenExcluding(any(LocalDate.class), any(), any(Pageable.class)))
                .thenReturn(exhibitions(4, 5));
        when(exhibitionRepository.findAnyExcluding(any(), any(Pageable.class)))
                .thenReturn(exhibitions(5, 6, 7, 8));

        List<Exhibition> result = recommendationService.recommend(USER_ID);

        Set<Long> uniqueIds = ids(result);
        assertThat(uniqueIds).hasSize(result.size());
        assertThat(uniqueIds).contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
    }

    private static List<Exhibition> exhibitions(long... ids) {
        return java.util.Arrays.stream(ids)
                .mapToObj(id -> Exhibition.builder()
                        .id(id)
                        .genre(GenreType.MODERN_ART)
                        .exhibitionTheme(ExhibitionTheme.CASUAL_VISIT)
                        .build())
                .toList();
    }

    private static Set<Long> ids(List<Exhibition> exhibitions) {
        return exhibitions.stream()
                .map(Exhibition::getId)
                .collect(Collectors.toSet());
    }
}
