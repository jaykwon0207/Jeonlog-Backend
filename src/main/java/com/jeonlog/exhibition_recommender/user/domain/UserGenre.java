package com.jeonlog.exhibition_recommender.user.domain;

import com.jeonlog.exhibition_recommender.exhibition.domain.Genre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_genres")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Float preferenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private UserLike userLike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private UserBookmark userBookmark;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Genre genre;
}