package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
import com.jeonlog.exhibition_recommender.user.domain.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserGenreRepository userGenreRepository;
    private final ExhibitionRepository exhibitionRepository;

    @Transactional
    protected UserGenre getOrCreateUserGenre(Long userId) {
        return userGenreRepository.findByUserId(userId).orElseGet(() -> {
            UserGenre ug = UserGenre.builder()
                    .userId(userId)
                    .build();
            // @PrePersist에서 모든 키를 0.0으로 채움
            return userGenreRepository.save(ug);
        });
    }

    /**
     * 추천 알고리즘:
     * 1) 장르/분위기 각각 상위 4개 추출
     * 2) 1~4등 랭크별로 (4,3,2,1개) 교집합 우선 랜덤 추천
     * 3) 교집합이 없다면 "장르 우선"으로 동일 수량 보강
     * 4) 중복 제거 후 최대 10개 반환
     */
    private static final int TARGET = 10;

    @Transactional(readOnly = true)
    public List<Exhibition> recommend(Long userId) {
        LocalDate today = LocalDate.now();

        var ug = userGenreRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("UserGenre가 없습니다. 초기화 필요"));

        var ranking = ug.rankingForRecommendation();
        List<GenreType> topGenres = ranking.getTopGenres4();
        List<ExhibitionTheme> topThemes = ranking.getTopThemes4();
        int[] pickCounts = ranking.getPickCounts(); // [4,3,2,1]

        List<Exhibition> result = new ArrayList<>(TARGET);
        Set<Long> pickedIds = new HashSet<>();

        // 1~4등: 교집합 우선 → 장르 보강
        for (int i = 0; i < pickCounts.length && result.size() < TARGET; i++) {
            int need = pickCounts[i];
            GenreType g = (i < topGenres.size()) ? topGenres.get(i) : null;
            ExhibitionTheme m = (i < topThemes.size()) ? topThemes.get(i) : null;
            if (need <= 0 || g == null) continue;

            // 1) 교집합
            if (m != null && need > 0) {
                var inter = exhibitionRepository.findActiveByGenreInAndMoodInExcluding(
                        today, List.of(g), List.of(m), emptySafe(pickedIds), PageRequest.of(0, need));
                need -= addAllUniq(result, pickedIds, inter, need);
            }

            // 2) 장르만
            if (need > 0) {
                var byGenre = exhibitionRepository.findActiveByGenreInExcluding(
                        today, List.of(g), emptySafe(pickedIds), PageRequest.of(0, need));
                addAllUniq(result, pickedIds, byGenre, need);
            }
        }

        // 3) 진행 중 랜덤 보충
        if (result.size() < TARGET) {
            int remain = TARGET - result.size();
            var rnd = exhibitionRepository.pickActiveRandomExcluding(today, emptySafe(pickedIds), remain);
            addAllUniq(result, pickedIds, rnd, remain);
        }

        // 4) 임박 예정(오늘~+60일) 랜덤 보충
        if (result.size() < TARGET) {
            int remain = TARGET - result.size();
            var rnd = exhibitionRepository.pickUpcomingRandomExcluding(
                    today, today.plusDays(60), emptySafe(pickedIds), remain);
            addAllUniq(result, pickedIds, rnd, remain);
        }

        // 5) 그래도 부족하면 전 범위 랜덤
        if (result.size() < TARGET) {
            int remain = TARGET - result.size();
            var rnd = exhibitionRepository.pickAnyRandomExcluding(emptySafe(pickedIds), remain);
            addAllUniq(result, pickedIds, rnd, remain);
        }

        return result;
    }

    private static int addAllUniq(List<Exhibition> dst, Set<Long> seen, List<Exhibition> src, int max) {
        int added = 0;
        for (Exhibition e : src) {
            if (e == null || e.getId() == null) continue;
            if (seen.add(e.getId())) {
                dst.add(e);
                if (++added >= max) break;
            }
        }
        return added;
    }

    private static Collection<Long> emptySafe(Set<Long> ids) {
        return (ids == null || ids.isEmpty()) ? List.of(-1L) : ids;
    }

    //특정 연령대 전시 추천
    private static final int AGE_LIMIT = 20;
    private static final int AGE_DAYS  = 30;

    @Transactional(readOnly = true)
    public List<Exhibition> recommendByAgeId(int ageId) {
        int[] band = toAgeBand(ageId); // [minAge, maxAge)
        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusDays(AGE_DAYS);
        LocalDate to    = today;

        int currentYear   = today.getYear();
        int minAge        = band[0];
        int maxAge        = band[1];
        int minBirthYear  = currentYear - maxAge + 1;
        int maxBirthYear  = currentYear - minAge;

        return exhibitionRepository.findPopularByAgeBand(
                today, from, to, minBirthYear, maxBirthYear,
                PageRequest.of(0, AGE_LIMIT)
        );
    }

    private static int[] toAgeBand(int ageId) {
        return switch (ageId) {
            case 10 -> new int[]{10, 20};
            case 20 -> new int[]{20, 30};
            case 30 -> new int[]{30, 40};
            case 40 -> new int[]{40, 50};
            case 50 -> new int[]{50, 200}; // 50+
            default -> throw new IllegalArgumentException("연령대 id는 10/20/30/40/50만 허용");
        };
    }

    //성별 별 전시 추천
    private static final int GENDER_LIMIT = 20;   // 최대 반환 개수
    private static final int GENDER_DAYS  = 30;   // 최근 30일 집계

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Exhibition> recommendByGender(Gender gender) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate from = today.minusDays(GENDER_DAYS);
        java.time.LocalDate to = today;

        return exhibitionRepository.findPopularByGender(
                today, from, to, gender,
                PageRequest.of(0, GENDER_LIMIT)
        );
    }

}
