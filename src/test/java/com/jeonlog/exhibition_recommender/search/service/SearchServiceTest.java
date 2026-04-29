package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Artist;
import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.exhibition.domain.Venue;
import com.jeonlog.exhibition_recommender.exhibition.domain.VenueType;
import com.jeonlog.exhibition_recommender.exhibition.repository.ArtistRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenueRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
import com.jeonlog.exhibition_recommender.search.dto.KeywordType;
import com.jeonlog.exhibition_recommender.search.dto.RecommendedKeywordDto;
import com.jeonlog.exhibition_recommender.search.repository.SearchRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchRepository searchRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGenreRepository userGenreRepository;
    @Mock
    private ExhibitionRepository exhibitionRepository;
    @Mock
    private VenueRepository venueRepository;
    @Mock
    private ArtistRepository artistRepository;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(
                searchRepository,
                userRepository,
                userGenreRepository,
                exhibitionRepository,
                venueRepository,
                artistRepository
        );
    }

    @Test
    void recordSearch_whenNormalizedKeywordIsBlank_doesNotPersist() {
        searchService.recordSearch("user@example.com", "!!!@@@");

        verifyNoInteractions(userRepository);
        verify(searchRepository, never()).save(any(Search.class));
    }

    @Test
    void recordSearch_whenValidQuery_persistsNormalizedKeyword() {
        User user = User.builder()
                .email("user@example.com")
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        searchService.recordSearch("user@example.com", "모네   !! 전시");

        ArgumentCaptor<Search> searchCaptor = ArgumentCaptor.forClass(Search.class);
        verify(searchRepository).save(searchCaptor.capture());
        assertThat(searchCaptor.getValue().getKeyword()).isEqualTo("모네 전시");
    }

    @Test
    void getTopKeywords_whenLimitIsZero_usesMinimumPageSizeOne() {
        LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
        when(searchRepository.aggregateKeywordCounts(eq(from), eq(to), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{"모네", 7L}));

        List<KeywordRankDto> result = searchService.getTopKeywords(from, to, 0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(searchRepository).aggregateKeywordCounts(eq(from), eq(to), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeyword()).isEqualTo("모네");
        assertThat(result.get(0).getCount()).isEqualTo(7L);
    }

    @Test
    void getTopKeywords_whenLimitExceedsMax_capsPageSizeToTen() {
        LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
        when(searchRepository.aggregateKeywordCounts(eq(from), eq(to), any(Pageable.class)))
                .thenReturn(List.of());

        searchService.getTopKeywords(from, to, 999);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(searchRepository).aggregateKeywordCounts(eq(from), eq(to), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getTopKeywords_mapsAggregatedRows() {
        LocalDateTime from = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 4, 30, 23, 59);
        when(searchRepository.aggregateKeywordCounts(eq(from), eq(to), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(
                        new Object[]{"모네 전시", 9L}
                ));

        List<KeywordRankDto> result = searchService.getTopKeywords(from, to, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeyword()).isEqualTo("모네 전시");
    }

    @Test
    void recordSearch_whenQueryHasPunctuation_replacesWithSpaceBeforePersist() {
        User user = User.builder()
                .email("user@example.com")
                .build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        searchService.recordSearch("user@example.com", "모네-전시");

        ArgumentCaptor<Search> searchCaptor = ArgumentCaptor.forClass(Search.class);
        verify(searchRepository).save(searchCaptor.capture());
        assertThat(searchCaptor.getValue().getKeyword()).isEqualTo("모네 전시");
    }

    @Test
    void getRecommendedKeywords_whenPersonalWeightsExist_scoresPreferredExhibitionHigher() {
        UserGenre ug = UserGenre.builder().userId(1L).build();
        ug.getGenreWeights().put(GenreType.MODERN_ART, 0.8);
        ug.getGenreWeights().put(GenreType.PHOTO, 0.1);
        ug.getThemeWeights().put(ExhibitionTheme.CASUAL_VISIT, 0.7);
        ug.getThemeWeights().put(ExhibitionTheme.QUIET_VIEWING, 0.1);
        when(userGenreRepository.findByUserId(1L)).thenReturn(Optional.of(ug));
        when(exhibitionRepository.findActiveByGenreInAndMoodInExcluding(any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        exhibition(1L, "하이 전시", "하이 미술관", "하이 작가", GenreType.MODERN_ART, ExhibitionTheme.CASUAL_VISIT)
                ));
        when(exhibitionRepository.findActiveByGenreInExcluding(any(), any(), any(), any()))
                .thenReturn(List.of(
                        exhibition(2L, "로우 전시", "로우 미술관", "로우 작가", GenreType.PHOTO, ExhibitionTheme.QUIET_VIEWING)
                ));
        when(exhibitionRepository.findActiveExcluding(any(), any(), any()))
                .thenReturn(List.of());
        when(searchRepository.aggregateKeywordCounts(any(), any(), any())).thenReturn(List.of());

        List<RecommendedKeywordDto> result = searchService.getRecommendedKeywords(1L, 8);

        RecommendedKeywordDto high = findByKeyword(result, "하이 전시");
        RecommendedKeywordDto low = findByKeyword(result, "로우 전시");
        assertThat(high).isNotNull();
        assertThat(low).isNotNull();
        assertThat(high.getScore()).isGreaterThan(low.getScore());
    }

    @Test
    void getRecommendedKeywords_deduplicatesAndAccumulatesSameKeyword() {
        UserGenre ug = userGenreWithWeights(GenreType.MODERN_ART, 0.6, ExhibitionTheme.CASUAL_VISIT, 0.6);
        when(userGenreRepository.findByUserId(1L)).thenReturn(Optional.of(ug));
        when(exhibitionRepository.findActiveByGenreInAndMoodInExcluding(any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        exhibition(1L, "공통 전시", "장소A", "작가A", GenreType.MODERN_ART, ExhibitionTheme.CASUAL_VISIT),
                        exhibition(2L, "공통 전시", "장소B", "작가B", GenreType.MODERN_ART, ExhibitionTheme.CASUAL_VISIT)
                ));
        when(exhibitionRepository.findActiveByGenreInExcluding(any(), any(), any(), any())).thenReturn(List.of());
        when(exhibitionRepository.findActiveExcluding(any(), any(), any())).thenReturn(List.of());
        when(searchRepository.aggregateKeywordCounts(any(), any(), any())).thenReturn(List.of());

        List<RecommendedKeywordDto> result = searchService.getRecommendedKeywords(1L, 8);

        long exhibitionKeywordCount = result.stream()
                .filter(dto -> dto.getType() == KeywordType.EXHIBITION && dto.getKeyword().equals("공통 전시"))
                .count();
        assertThat(exhibitionKeywordCount).isEqualTo(1);
    }

    @Test
    void getRecommendedKeywords_whenEnoughCandidates_reservesAtLeastTwoPerType() {
        UserGenre ug = userGenreWithWeights(GenreType.MODERN_ART, 0.9, ExhibitionTheme.CASUAL_VISIT, 0.9);
        when(userGenreRepository.findByUserId(1L)).thenReturn(Optional.of(ug));
        when(exhibitionRepository.findActiveByGenreInAndMoodInExcluding(any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        exhibition(1L, "전시1", "장소1", "작가1", GenreType.MODERN_ART, ExhibitionTheme.CASUAL_VISIT),
                        exhibition(2L, "전시2", "장소2", "작가2", GenreType.MODERN_ART, ExhibitionTheme.CASUAL_VISIT),
                        exhibition(3L, "전시3", "장소3", "작가3", GenreType.MODERN_ART, ExhibitionTheme.CASUAL_VISIT)
                ));
        when(exhibitionRepository.findActiveByGenreInExcluding(any(), any(), any(), any())).thenReturn(List.of());
        when(exhibitionRepository.findActiveExcluding(any(), any(), any())).thenReturn(List.of());
        when(searchRepository.aggregateKeywordCounts(any(), any(), any())).thenReturn(List.of());

        List<RecommendedKeywordDto> result = searchService.getRecommendedKeywords(1L, 8);

        long exhibitionCount = result.stream().filter(dto -> dto.getType() == KeywordType.EXHIBITION).count();
        long venueCount = result.stream().filter(dto -> dto.getType() == KeywordType.VENUE).count();
        long artistCount = result.stream().filter(dto -> dto.getType() == KeywordType.ARTIST).count();

        assertThat(exhibitionCount).isGreaterThanOrEqualTo(2);
        assertThat(venueCount).isGreaterThanOrEqualTo(2);
        assertThat(artistCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getRecommendedKeywords_whenPersonalizedCandidatesAreMissing_fallsBackToPopularWithTypeInference() {
        when(userGenreRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(searchRepository.aggregateKeywordCounts(any(), any(), any())).thenReturn(List.of(
                new Object[]{"서울시립미술관", 10L},
                new Object[]{"김환기", 8L},
                new Object[]{"모네전", 7L}
        ));
        when(venueRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(venueRepository.existsByNameContainingIgnoreCase(anyString())).thenReturn(false);
        when(artistRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(artistRepository.existsByNameContainingIgnoreCase(anyString())).thenReturn(false);
        when(venueRepository.existsByNameIgnoreCase("서울시립미술관")).thenReturn(true);
        when(artistRepository.existsByNameIgnoreCase("김환기")).thenReturn(true);

        List<RecommendedKeywordDto> result = searchService.getRecommendedKeywords(1L, 8);

        assertThat(result).hasSize(3);
        assertThat(findByKeyword(result, "서울시립미술관").getType()).isEqualTo(KeywordType.VENUE);
        assertThat(findByKeyword(result, "김환기").getType()).isEqualTo(KeywordType.ARTIST);
        assertThat(findByKeyword(result, "모네전").getType()).isEqualTo(KeywordType.EXHIBITION);
    }

    @Test
    void getRecommendedKeywords_clampsLimitRange() {
        when(userGenreRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(searchRepository.aggregateKeywordCounts(any(), any(), any())).thenReturn(List.of(
                new Object[]{"키워드1", 10L},
                new Object[]{"키워드2", 9L},
                new Object[]{"키워드3", 8L},
                new Object[]{"키워드4", 7L},
                new Object[]{"키워드5", 6L},
                new Object[]{"키워드6", 5L},
                new Object[]{"키워드7", 4L},
                new Object[]{"키워드8", 3L},
                new Object[]{"키워드9", 2L},
                new Object[]{"키워드10", 1L},
                new Object[]{"키워드11", 1L}
        ));

        List<RecommendedKeywordDto> minResult = searchService.getRecommendedKeywords(1L, 0);
        List<RecommendedKeywordDto> maxResult = searchService.getRecommendedKeywords(1L, 20);

        assertThat(minResult).hasSize(1);
        assertThat(maxResult.size()).isLessThanOrEqualTo(10);
    }

    private static UserGenre userGenreWithWeights(
            GenreType genre,
            double genreWeight,
            ExhibitionTheme theme,
            double themeWeight
    ) {
        UserGenre ug = UserGenre.builder().userId(1L).build();
        ug.getGenreWeights().put(genre, genreWeight);
        ug.getThemeWeights().put(theme, themeWeight);
        return ug;
    }

    private static Exhibition exhibition(
            Long id,
            String title,
            String venueName,
            String artistName,
            GenreType genre,
            ExhibitionTheme theme
    ) {
        Venue venue = Venue.builder().name(venueName).type(VenueType.GALLERY).build();
        Artist artist = Artist.builder().name(artistName).build();
        return Exhibition.builder()
                .id(id)
                .title(title)
                .genre(genre)
                .exhibitionTheme(theme)
                .venue(venue)
                .artists(List.of(artist))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .build();
    }

    private static RecommendedKeywordDto findByKeyword(List<RecommendedKeywordDto> list, String keyword) {
        return list.stream()
                .filter(dto -> dto.getKeyword().equals(keyword))
                .findFirst()
                .orElse(null);
    }
}
