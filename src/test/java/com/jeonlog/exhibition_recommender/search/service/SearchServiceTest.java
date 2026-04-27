package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(searchRepository, userRepository);
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
}
