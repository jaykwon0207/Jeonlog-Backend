package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.search.domain.Search;
import com.jeonlog.exhibition_recommender.exhibition.repository.ArtistRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.exhibition.repository.VenueRepository;
import com.jeonlog.exhibition_recommender.search.dto.KeywordRankDto;
import com.jeonlog.exhibition_recommender.search.repository.SearchRepository;
import com.jeonlog.exhibition_recommender.user.domain.User;
import com.jeonlog.exhibition_recommender.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;

    /**
     * 검색 기록 저장
     */
    @Transactional
    public void recordSearch(String email, String query) {
        if (query == null || query.isBlank() || email == null) return;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        // 검색어 전체를 하나의 키워드로 처리
        String keyword = normalize(query);
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
        List<Object[]> rows = searchRepository.aggregateKeywordCounts(from, to);

        return rows.stream()
                .map(r -> KeywordRankDto.builder()
                        .keyword((String) r[0])
                        .count(((Number) r[1]).longValue())
                        .build())
                .filter(dto -> isValidKeyword(dto.getKeyword()))
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());
    }

    /**
     * 실제 존재하는 전시/작가/공간 키워드인지 확인
     */
    private boolean isValidKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return false;
        String lowered = keyword.toLowerCase();

        return exhibitionRepository.existsByTitleContainingIgnoreCase(lowered)
                || venueRepository.existsByNameContainingIgnoreCase(lowered)
                || artistRepository.existsByNameContainingIgnoreCase(lowered);
    }

    /**
     * 문자열 정규화 — 소문자 변환 + 특수문자 제거
     */
    private String normalize(String keyword) {
        return keyword.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", "")
                .trim();
    }
}
