package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
import com.jeonlog.exhibition_recommender.search.repository.SearchRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_KEYWORD_RANK_LIMIT = 10;

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;

    /**
     * 검색 기록 저장
     */
    @Transactional
    public void recordSearch(String email, String query) {
        if (query == null || email == null) return;

        String keyword = normalize(query);
        if (keyword.isBlank()) return;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        LocalDateTime now = LocalDateTime.now();

        Search search = Search.builder()
                .user(user)
                .keyword(keyword)
                .searchedAt(now)
                .build();

        searchRepository.save(search);
    }

    /**
     *인기 검색어 랭킹 조회
     */
    @Transactional(readOnly = true)
    public List<KeywordRankDto> getTopKeywords(LocalDateTime from, LocalDateTime to, int limit) {
        int safeLimit = Math.min(MAX_KEYWORD_RANK_LIMIT, Math.max(1, limit));
        List<Object[]> rows = searchRepository.aggregateKeywordCounts(
                from,
                to,
                PageRequest.of(0, safeLimit)
        );

        return rows.stream()
                .map(row -> KeywordRankDto.builder()
                        .keyword((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 문자열 정규화 — 소문자 변환 + 특수문자 제거
     */
    private String normalize(String keyword) {
        return keyword.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
