package com.jeonlog.exhibition_recommender.recommendation.service;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import com.jeonlog.exhibition_recommender.exhibition.repository.ExhibitionRepository;
import com.jeonlog.exhibition_recommender.recommendation.domain.UserGenre;
import com.jeonlog.exhibition_recommender.recommendation.repository.UserGenreRepository;
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
    protected UserGenre getOrCreate(Long userId) {
        return userGenreRepository.findByUserId(userId).orElseGet(() ->
                userGenreRepository.save(UserGenre.builder().userId(userId).build())
        );
    }

    @Transactional
    public List<Exhibition> recommend(Long userId) {
        LocalDate today = LocalDate.now();

        UserGenre ug = getOrCreate(userId);

        var ranking = ug.rankingForRecommendation();
        List<GenreType> topGenres = ranking.getTopGenres4();
        List<ExhibitionTheme> topMoods = ranking.getTopMoods4();
        int[] pickCounts = ranking.getPickCounts();

        List<Exhibition> result = new ArrayList<>();
        Set<Long> picked = new HashSet<>();

        for (int i = 0; i < pickCounts.length && result.size() < 10; i++) {
            int need = pickCounts[i];
            GenreType g = i < topGenres.size() ? topGenres.get(i) : null;
            ExhibitionTheme m = i < topMoods.size() ? topMoods.get(i) : null;

            if (g == null) continue;

            if (m != null) {
                var inter = exhibitionRepository.findActiveByGenreInAndMoodInExcluding(
                        today, List.of(g), List.of(m), empty(picked), PageRequest.of(0, need)
                );
                need -= add(result, picked, inter, need);
            }

            if (need > 0) {
                var byGenre = exhibitionRepository.findActiveByGenreInExcluding(
                        today, List.of(g), empty(picked), PageRequest.of(0, need)
                );
                add(result, picked, byGenre, need);
            }
        }

        if (result.size() < 10) {
            int r = 10 - result.size();
            var rnd = exhibitionRepository.pickActiveRandomExcluding(today, empty(picked), r);
            add(result, picked, rnd, r);
        }

        if (result.size() < 10) {
            int r = 10 - result.size();
            var rnd = exhibitionRepository.pickUpcomingRandomExcluding(
                    today, today.plusDays(60), empty(picked), r
            );
            add(result, picked, rnd, r);
        }

        if (result.size() < 10) {
            int r = 10 - result.size();
            var rnd = exhibitionRepository.pickAnyRandomExcluding(today, empty(picked), r);
            add(result, picked, rnd, r);
        }

        return result;
    }

    private static int add(List<Exhibition> dst, Set<Long> seen, List<Exhibition> src, int max) {
        int count = 0;
        for (Exhibition e : src) {
            if (e == null || e.getId() == null) continue;
            if (seen.add(e.getId())) {
                dst.add(e);
                if (++count >= max) break;
            }
        }
        return count;
    }

    private static Collection<Long> empty(Set<Long> ids) {
        return ids == null || ids.isEmpty() ? List.of(-1L) : ids;
    }
}
