package com.jeonlog.exhibition_recommender.search.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_KEYWORD_RANK_LIMIT = 10;
    private static final int DEFAULT_RECOMMEND_LIMIT = 8;
    private static final int MAX_RECOMMEND_LIMIT = 10;
    private static final int TYPE_MIN_QUOTA = 2;
    private static final int PERSONAL_CANDIDATE_FETCH_SIZE = 120;
    private static final int POPULAR_KEYWORD_FETCH_SIZE = 30;
    private static final double GENRE_WEIGHT_FACTOR = 0.6;
    private static final double THEME_WEIGHT_FACTOR = 0.4;
    private static final double POPULAR_BOOST_FACTOR = 0.35;
    private static final double EXHIBITION_TYPE_WEIGHT_FACTOR = 0.8;
    private static final double VENUE_TYPE_WEIGHT_FACTOR = 1.0;
    private static final double ARTIST_TYPE_WEIGHT_FACTOR = 1.0;
    private static final Collection<Long> NO_EXCLUDE_IDS = List.of(-1L);

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;
    private final UserGenreRepository userGenreRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;

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

    @Transactional(readOnly = true)
    public List<RecommendedKeywordDto> getRecommendedKeywords(Long userId, Integer limit) {
        int safeLimit = clampRecommendLimit(limit);

        UserGenre userGenre = (userId == null)
                ? null
                : userGenreRepository.findByUserId(userId).orElse(null);

        Map<String, KeywordScoreAccumulator> accumulators = new HashMap<>();
        if (hasPersonalWeight(userGenre)) {
            List<Exhibition> candidates = fetchPersonalizedCandidates(userGenre);
            for (Exhibition exhibition : candidates) {
                double personalBaseScore = calculatePersonalBaseScore(userGenre, exhibition);
                if (personalBaseScore <= 0) continue;

                addKeywordContribution(
                        accumulators,
                        exhibition.getTitle(),
                        KeywordType.EXHIBITION,
                        personalBaseScore * EXHIBITION_TYPE_WEIGHT_FACTOR
                );
                if (exhibition.getVenue() != null) {
                    addKeywordContribution(
                            accumulators,
                            exhibition.getVenue().getName(),
                            KeywordType.VENUE,
                            personalBaseScore * VENUE_TYPE_WEIGHT_FACTOR
                    );
                }
                if (exhibition.getArtists() != null) {
                    exhibition.getArtists().forEach(artist ->
                            addKeywordContribution(
                                    accumulators,
                                    artist.getName(),
                                    KeywordType.ARTIST,
                                    personalBaseScore * ARTIST_TYPE_WEIGHT_FACTOR
                            ));
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<Object[]> popularRows = searchRepository.aggregateKeywordCounts(
                now.minusDays(30),
                now,
                PageRequest.of(0, POPULAR_KEYWORD_FETCH_SIZE)
        );
        applyPopularBoost(accumulators, popularRows);

        List<RecommendedKeywordDto> personalizedDtos = accumulators.values().stream()
                .map(this::toRecommendedKeywordDto)
                .sorted(recommendationOrder())
                .toList();

        List<PopularKeywordCandidate> popularCandidates = buildPopularCandidates(popularRows);
        return assembleRecommendationList(personalizedDtos, popularCandidates, safeLimit);
    }

    private int clampRecommendLimit(Integer limit) {
        int requested = (limit == null) ? DEFAULT_RECOMMEND_LIMIT : limit;
        return Math.min(MAX_RECOMMEND_LIMIT, Math.max(1, requested));
    }

    private boolean hasPersonalWeight(UserGenre userGenre) {
        return userGenre != null
                && (userGenre.totalGenreWeight() > 0.0 || userGenre.totalMoodWeight() > 0.0);
    }

    private List<Exhibition> fetchPersonalizedCandidates(UserGenre userGenre) {
        LocalDate today = LocalDate.now();
        List<GenreType> topGenres = userGenre.topGenres(4);
        List<ExhibitionTheme> topThemes = userGenre.topMoods(4);

        List<Exhibition> merged = new ArrayList<>();
        Set<Long> seenIds = new java.util.HashSet<>();

        if (!topGenres.isEmpty() && !topThemes.isEmpty()) {
            addUniqueExhibitions(
                    merged,
                    seenIds,
                    exhibitionRepository.findActiveByGenreInAndMoodInExcluding(
                            today,
                            topGenres,
                            topThemes,
                            NO_EXCLUDE_IDS,
                            PageRequest.of(0, PERSONAL_CANDIDATE_FETCH_SIZE)
                    )
            );
        }

        if (!topGenres.isEmpty() && merged.size() < PERSONAL_CANDIDATE_FETCH_SIZE) {
            addUniqueExhibitions(
                    merged,
                    seenIds,
                    exhibitionRepository.findActiveByGenreInExcluding(
                            today,
                            topGenres,
                            NO_EXCLUDE_IDS,
                            PageRequest.of(0, PERSONAL_CANDIDATE_FETCH_SIZE)
                    )
            );
        }

        if (merged.size() < PERSONAL_CANDIDATE_FETCH_SIZE) {
            addUniqueExhibitions(
                    merged,
                    seenIds,
                    exhibitionRepository.findActiveExcluding(
                            today,
                            NO_EXCLUDE_IDS,
                            PageRequest.of(0, PERSONAL_CANDIDATE_FETCH_SIZE)
                    )
            );
        }

        return merged;
    }

    private void addUniqueExhibitions(List<Exhibition> merged, Set<Long> seenIds, List<Exhibition> source) {
        for (Exhibition exhibition : source) {
            if (exhibition == null || exhibition.getId() == null) continue;
            if (seenIds.add(exhibition.getId())) {
                merged.add(exhibition);
                if (merged.size() >= PERSONAL_CANDIDATE_FETCH_SIZE) {
                    return;
                }
            }
        }
    }

    private double calculatePersonalBaseScore(UserGenre userGenre, Exhibition exhibition) {
        if (userGenre == null || exhibition == null) return 0.0;
        double genreWeight = userGenre.getGenreWeights().getOrDefault(exhibition.getGenre(), 0.0);
        double themeWeight = userGenre.getThemeWeights().getOrDefault(exhibition.getExhibitionTheme(), 0.0);
        return (genreWeight * GENRE_WEIGHT_FACTOR) + (themeWeight * THEME_WEIGHT_FACTOR);
    }

    private void addKeywordContribution(
            Map<String, KeywordScoreAccumulator> accumulators,
            String keyword,
            KeywordType type,
            double contribution
    ) {
        if (keyword == null || contribution <= 0) return;
        String normalized = normalize(keyword);
        if (normalized.isBlank()) return;

        KeywordScoreAccumulator acc = accumulators.computeIfAbsent(
                normalized,
                key -> new KeywordScoreAccumulator(normalized)
        );
        acc.addScore(keyword.trim(), type, contribution);
    }

    private void applyPopularBoost(Map<String, KeywordScoreAccumulator> accumulators, List<Object[]> popularRows) {
        int size = popularRows.size();
        if (size == 0) return;

        for (int i = 0; i < size; i++) {
            String rawKeyword = safeString(popularRows.get(i));
            String normalized = normalize(rawKeyword);
            if (normalized.isBlank()) continue;

            double normalizedRank = (size - i) / (double) size;
            double boost = normalizedRank * POPULAR_BOOST_FACTOR;

            KeywordScoreAccumulator acc = accumulators.get(normalized);
            if (acc != null) {
                acc.addBoost(boost);
            }
        }
    }

    private List<PopularKeywordCandidate> buildPopularCandidates(List<Object[]> popularRows) {
        int size = popularRows.size();
        if (size == 0) return List.of();

        Map<String, PopularKeywordCandidate> deduplicated = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String rawKeyword = safeString(popularRows.get(i));
            String normalized = normalize(rawKeyword);
            if (normalized.isBlank()) continue;

            double normalizedRank = (size - i) / (double) size;
            double score = normalizedRank * POPULAR_BOOST_FACTOR;

            deduplicated.putIfAbsent(
                    normalized,
                    new PopularKeywordCandidate(rawKeyword.trim(), inferKeywordType(rawKeyword), score)
            );
        }
        return new ArrayList<>(deduplicated.values());
    }

    private KeywordType inferKeywordType(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.isBlank()) {
            return KeywordType.EXHIBITION;
        }
        String keyword = rawKeyword.trim();

        if (venueRepository.existsByNameIgnoreCase(keyword) || venueRepository.existsByNameContainingIgnoreCase(keyword)) {
            return KeywordType.VENUE;
        }
        if (artistRepository.existsByNameIgnoreCase(keyword) || artistRepository.existsByNameContainingIgnoreCase(keyword)) {
            return KeywordType.ARTIST;
        }
        return KeywordType.EXHIBITION;
    }

    private List<RecommendedKeywordDto> assembleRecommendationList(
            List<RecommendedKeywordDto> personalizedDtos,
            List<PopularKeywordCandidate> popularCandidates,
            int limit
    ) {
        Map<String, RecommendedKeywordDto> selectedByNormalized = new LinkedHashMap<>();
        Map<KeywordType, Integer> selectedTypeCounts = initializeTypeCountMap();

        selectByTypeQuota(personalizedDtos, selectedByNormalized, selectedTypeCounts, limit);
        fillByScoreOrder(personalizedDtos, selectedByNormalized, selectedTypeCounts, limit);
        fillMissingTypeQuotaWithPopular(popularCandidates, selectedByNormalized, selectedTypeCounts, limit);
        fillWithPopularFallback(popularCandidates, selectedByNormalized, selectedTypeCounts, limit);

        return selectedByNormalized.values().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Map<KeywordType, Integer> initializeTypeCountMap() {
        Map<KeywordType, Integer> counts = new EnumMap<>(KeywordType.class);
        counts.put(KeywordType.EXHIBITION, 0);
        counts.put(KeywordType.VENUE, 0);
        counts.put(KeywordType.ARTIST, 0);
        return counts;
    }

    private void selectByTypeQuota(
            List<RecommendedKeywordDto> personalizedDtos,
            Map<String, RecommendedKeywordDto> selectedByNormalized,
            Map<KeywordType, Integer> selectedTypeCounts,
            int limit
    ) {
        for (KeywordType type : KeywordType.values()) {
            for (RecommendedKeywordDto dto : personalizedDtos) {
                if (selectedByNormalized.size() >= limit) return;
                if (dto.getType() != type) continue;
                if (selectedTypeCounts.get(type) >= TYPE_MIN_QUOTA) break;
                tryAdd(dto, selectedByNormalized, selectedTypeCounts);
            }
        }
    }

    private void fillByScoreOrder(
            List<RecommendedKeywordDto> personalizedDtos,
            Map<String, RecommendedKeywordDto> selectedByNormalized,
            Map<KeywordType, Integer> selectedTypeCounts,
            int limit
    ) {
        for (RecommendedKeywordDto dto : personalizedDtos) {
            if (selectedByNormalized.size() >= limit) return;
            tryAdd(dto, selectedByNormalized, selectedTypeCounts);
        }
    }

    private void fillMissingTypeQuotaWithPopular(
            List<PopularKeywordCandidate> popularCandidates,
            Map<String, RecommendedKeywordDto> selectedByNormalized,
            Map<KeywordType, Integer> selectedTypeCounts,
            int limit
    ) {
        for (KeywordType type : KeywordType.values()) {
            if (selectedTypeCounts.get(type) >= TYPE_MIN_QUOTA) continue;

            for (PopularKeywordCandidate candidate : popularCandidates) {
                if (selectedByNormalized.size() >= limit) return;
                if (selectedTypeCounts.get(type) >= TYPE_MIN_QUOTA) break;
                if (candidate.type() != type) continue;

                tryAdd(
                        RecommendedKeywordDto.builder()
                                .keyword(candidate.keyword())
                                .type(candidate.type())
                                .score(candidate.score())
                                .build(),
                        selectedByNormalized,
                        selectedTypeCounts
                );
            }
        }
    }

    private void fillWithPopularFallback(
            List<PopularKeywordCandidate> popularCandidates,
            Map<String, RecommendedKeywordDto> selectedByNormalized,
            Map<KeywordType, Integer> selectedTypeCounts,
            int limit
    ) {
        for (PopularKeywordCandidate candidate : popularCandidates) {
            if (selectedByNormalized.size() >= limit) return;

            tryAdd(
                    RecommendedKeywordDto.builder()
                            .keyword(candidate.keyword())
                            .type(candidate.type())
                            .score(candidate.score())
                            .build(),
                    selectedByNormalized,
                    selectedTypeCounts
            );
        }
    }

    private void tryAdd(
            RecommendedKeywordDto dto,
            Map<String, RecommendedKeywordDto> selectedByNormalized,
            Map<KeywordType, Integer> selectedTypeCounts
    ) {
        if (dto == null || dto.getKeyword() == null) return;
        String normalized = normalize(dto.getKeyword());
        if (normalized.isBlank() || selectedByNormalized.containsKey(normalized)) return;

        selectedByNormalized.put(normalized, dto);
        selectedTypeCounts.put(dto.getType(), selectedTypeCounts.get(dto.getType()) + 1);
    }

    private Comparator<RecommendedKeywordDto> recommendationOrder() {
        return Comparator.comparingDouble(RecommendedKeywordDto::getScore).reversed()
                .thenComparing(RecommendedKeywordDto::getKeyword, String.CASE_INSENSITIVE_ORDER);
    }

    private RecommendedKeywordDto toRecommendedKeywordDto(KeywordScoreAccumulator acc) {
        return RecommendedKeywordDto.builder()
                .keyword(acc.displayKeyword())
                .type(acc.decideType())
                .score(acc.totalScore())
                .build();
    }

    private String safeString(Object[] row) {
        if (row == null || row.length == 0 || row[0] == null) return "";
        return String.valueOf(row[0]);
    }

    /**
     * 문자열 정규화 — 소문자 변환 + 특수문자 제거
     */
    private String normalize(String keyword) {
        if (keyword == null) return "";
        return keyword.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static final class KeywordScoreAccumulator {
        private final String normalizedKeyword;
        private final Map<KeywordType, Double> typeScoreMap = new EnumMap<>(KeywordType.class);
        private String displayKeyword;
        private double totalScore;

        private KeywordScoreAccumulator(String normalizedKeyword) {
            this.normalizedKeyword = normalizedKeyword;
        }

        private void addScore(String displayKeyword, KeywordType type, double score) {
            if (displayKeyword == null || displayKeyword.isBlank() || score <= 0) return;
            if (this.displayKeyword == null || this.displayKeyword.isBlank()) {
                this.displayKeyword = displayKeyword.trim();
            }

            totalScore += score;
            typeScoreMap.merge(type, score, Double::sum);
        }

        private void addBoost(double boost) {
            if (boost > 0) {
                totalScore += boost;
            }
        }

        private KeywordType decideType() {
            return typeScoreMap.entrySet().stream()
                    .max(Comparator.<Map.Entry<KeywordType, Double>>comparingDouble(Map.Entry::getValue)
                            .thenComparingInt(entry -> typePriority(entry.getKey())))
                    .map(Map.Entry::getKey)
                    .orElse(KeywordType.EXHIBITION);
        }

        private String displayKeyword() {
            return (displayKeyword == null || displayKeyword.isBlank()) ? normalizedKeyword : displayKeyword;
        }

        private double totalScore() {
            return totalScore;
        }

        private int typePriority(KeywordType type) {
            return switch (type) {
                case VENUE -> 3;
                case ARTIST -> 2;
                case EXHIBITION -> 1;
            };
        }
    }

    private record PopularKeywordCandidate(
            String keyword,
            KeywordType type,
            double score
    ) {
    }
}
