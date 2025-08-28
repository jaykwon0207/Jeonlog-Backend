package com.jeonlog.exhibition_recommender.recommendation.domain;

import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionMood;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자별 장르/분위기 선호도(가중치) 보관 엔티티.
 * - 장르/분위기별 가중치 누적(Bookmark/RecordLike/ExhibitionRecord)
 * - 합계가 1을 넘으면, 현재 랭킹을 반영하여 재분배(초기화)
 * - 상위 랭킹 리스트 제공
 */
@Entity
@Table(name = "user_genres",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_genres_user", columnNames = "user_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserGenre {

    public static final double MAX_SUM = 1.0;
    public static final double DELTA_BOOKMARK = 0.02;       // Bookmark
    public static final double DELTA_RECORD_LIKE = 0.01;    // RecordLike
    public static final double DELTA_EXHIBITION_RECORD = 0.03; // ExhibitionRecord

    // 초기화 시 랭킹별 기본 분포(1~4위)
    private static final double[] RESET_BASE = new double[]{0.04, 0.03, 0.02, 0.01};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    //장르 가중치
    @ElementCollection
    @CollectionTable(name = "user_genre_weights", joinColumns = @JoinColumn(name = "user_genre_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Map<GenreType, Double> genreWeights = new EnumMap<>(GenreType.class);

    //전시분위기 가중치
    @ElementCollection
    @CollectionTable(name = "user_mood_weights", joinColumns = @JoinColumn(name = "user_genre_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Map<ExhibitionMood, Double> moodWeights = new EnumMap<>(ExhibitionMood.class);

    //생성 시 전체 키 0.0으로 초기 세팅
    @PrePersist
    void onCreate() {
        if (genreWeights == null) genreWeights = new EnumMap<>(GenreType.class);
        if (moodWeights == null)  moodWeights  = new EnumMap<>(ExhibitionMood.class);
        for (GenreType g : GenreType.values()) genreWeights.putIfAbsent(g, 0.0);
        for (ExhibitionMood m : ExhibitionMood.values()) moodWeights.putIfAbsent(m, 0.0);
    }

    public void addFromBookmark(GenreType genre, ExhibitionMood mood) {
        bump(genre, mood, DELTA_BOOKMARK);
    }

    public void addFromRecordLike(GenreType genre, ExhibitionMood mood) {
        bump(genre, mood, DELTA_RECORD_LIKE);
    }

    public void addFromExhibitionRecord(GenreType genre, ExhibitionMood mood) {
        bump(genre, mood, DELTA_EXHIBITION_RECORD);
    }

    //가중치 증가 로직
    private void bump(GenreType genre, ExhibitionMood mood, double delta) {
        genreWeights.put(genre, Math.max(0.0, genreWeights.getOrDefault(genre, 0.0) + delta));
        moodWeights.put(mood,   Math.max(0.0, moodWeights.getOrDefault(mood, 0.0) + delta));

        //장르 가중치와 전시분위기 가중치 중 어느 한쪽이라도 1 초과면 초기화
        if (totalGenreWeight() > MAX_SUM || totalMoodWeight() > MAX_SUM) {
            resetByCurrentRanking();
        }
    }

    //가중치 리스트 별 합계
    public double totalGenreWeight() {
        return genreWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    public double totalMoodWeight() {
        return moodWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * 초기화:
     *  - 현재 랭킹을 보존하면서 1~4위에 RESET_BASE(0.04/0.03/0.02/0.01) 부여, 나머지 0
     *  - 장르와 분위기 각각 독립적으로 적용
     *  - 최종 합이 1을 넘으면 비율 스케일링
     */
    public void resetByCurrentRanking() {
        applyBaseByRankingForGenres(RESET_BASE);
        applyBaseByRankingForMoods(RESET_BASE);

        double gSum = totalGenreWeight();
        if (gSum > MAX_SUM && gSum > 0) {
            double scaleG = MAX_SUM / gSum;
            genreWeights.replaceAll((k, v) -> v * scaleG);
        }

        double mSum = totalMoodWeight();
        if (mSum > MAX_SUM && mSum > 0) {
            double scaleM = MAX_SUM / mSum;
            moodWeights.replaceAll((k, v) -> v * scaleM);
        }
    }

    public void revertExhibitionRecord(GenreType genre, ExhibitionMood mood) {
        bumpNegative(genre, mood, DELTA_EXHIBITION_RECORD); // 0.03 감소
    }

    public void revertRecordLike(GenreType genre, ExhibitionMood mood) {
        bumpNegative(genre, mood, DELTA_RECORD_LIKE); // 0.01 감소
    }

    public void revertBookmark(GenreType genre, ExhibitionMood mood) {
        bumpNegative(genre, mood, DELTA_BOOKMARK); // 0.02 감소
    }

    // 가중치 감소 로직
    private void bumpNegative(GenreType genre, ExhibitionMood mood, double delta) {
        double g = Math.max(0.0, genreWeights.getOrDefault(genre, 0.0) - delta);
        double m = Math.max(0.0, moodWeights.getOrDefault(mood, 0.0) - delta);
        genreWeights.put(genre, g);
        moodWeights.put(mood, m);
    }


    // 장르: 랭킹에 따라 RESET_BASE 분포로 재배치
    private void applyBaseByRankingForGenres(double[] base) {
        List<GenreType> order = topGenres(); // 내림차순 전체
        Map<GenreType, Double> newMap = new EnumMap<>(GenreType.class);
        for (int i = 0; i < order.size(); i++) {
            double v = (i < base.length) ? base[i] : 0.0;
            newMap.put(order.get(i), v);
        }
        for (GenreType g : GenreType.values()) newMap.putIfAbsent(g, 0.0);
        genreWeights = newMap;
    }

    // 분위기: 랭킹에 따라 RESET_BASE 분포로 재배치
    private void applyBaseByRankingForMoods(double[] base) {
        List<ExhibitionMood> order = topMoods(); // 내림차순 전체
        Map<ExhibitionMood, Double> newMap = new EnumMap<>(ExhibitionMood.class);
        for (int i = 0; i < order.size(); i++) {
            double v = (i < base.length) ? base[i] : 0.0;
            newMap.put(order.get(i), v);
        }
        for (ExhibitionMood m : ExhibitionMood.values()) newMap.putIfAbsent(m, 0.0);
        moodWeights = newMap;
    }

    // 장르 전체 랭킹 (값 큰순)
    public List<GenreType> topGenres() {
        return genreWeights.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    // 분위기 전체 랭킹 (값 큰순)
    public List<ExhibitionMood> topMoods() {
        return moodWeights.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<GenreType> topGenres(int n) {
        return genreWeights.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<ExhibitionMood> topMoods(int n) {
        return moodWeights.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public static int[] rankPickCounts() {
        return new int[]{4, 3, 2, 1};
    }

    public RecommendationRanking rankingForRecommendation() {
        return new RecommendationRanking(this.topGenres(4), this.topMoods(4), rankPickCounts());
    }

    @Getter
    @AllArgsConstructor
    public static class RecommendationRanking {
        private final List<GenreType> topGenres4;        // 1~4등
        private final List<ExhibitionMood> topMoods4;    // 1~4등
        private final int[] pickCounts;                  // [4,3,2,1]
    }
}
