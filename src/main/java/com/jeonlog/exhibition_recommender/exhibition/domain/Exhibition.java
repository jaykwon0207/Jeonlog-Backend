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

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    // 전시가 열리는 세부 전시실/관 이름 (주소/위도/경도는 Venue에서 관리)
    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 500)
    private String posterUrl;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_free", nullable = false)
    private boolean isFree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExhibitionMood exhibitionMood;

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

    // 장소(미술관/박물관) 참조 – 세부 전시실명은 location에 별도 저장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
}
