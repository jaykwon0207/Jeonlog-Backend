package com.jeonlog.exhibition_recommender.exhibition.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "exhibitions",
        indexes = {
                @Index(name = "idx_exhibition_start_date", columnList = "startDate"),
                @Index(name = "idx_exhibition_end_date", columnList = "endDate")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Exhibition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 전시 이름
    private String title;

    @Lob
    @Column(nullable = false, length = 500) // 전시 요약
    private String description;

    @Column(nullable = false) // 전시 위치
    private String location;

    @Column(nullable = false, length = 500) // 포스터 URL
    private String posterUrl;

    @Column(nullable = false) // 시작 날짜
    private LocalDate startDate;

    @Column(nullable = false) // 종료 날짜
    private LocalDate endDate;

    @Column(nullable = false) // 관람료
    private int price;

    @Column(name = "is_free", nullable = false) // 무료 여부
    private boolean isFree;

    @Enumerated(EnumType.STRING) // 테마
    @Column(nullable = false, length = 50)
    private ExhibitionTheme exhibitionTheme;

    @Enumerated(EnumType.STRING) // 장르
    @Column(nullable = false, length = 50)
    private GenreType genre;

    @Column(length = 100, nullable = false) // 문의
    private String contact;

    @Column(length = 500, nullable = false) // 사이트 URL
    private String website;

    @Column(length = 200, nullable = false) // 관람시간
    private String viewingTime;

    @Column(length = 200, nullable = false) // 개인 맞춤 전시 추천 포스터 URL
    private String personalizedPosterUrl;

    @Column(nullable = false, length = 500) // 전체 추천 포스터 URL
    private String generalRecommendationsPosterUrl;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "exhibition_artists",
            joinColumns = @JoinColumn(name = "exhibition_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_exhibition_artist",
                    columnNames = {"exhibition_id", "artist_id"}
            )
    )
    private List<Artist> artists = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "exhibition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExhibitionGenre> exhibitionGenres = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // ✅ 포스터 URL 업데이트 메서드 추가
    public void updatePosterUrl(String newUrl) {
        this.posterUrl = newUrl;
    }

}