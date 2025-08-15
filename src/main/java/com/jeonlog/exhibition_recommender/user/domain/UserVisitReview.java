package com.jeonlog.exhibition_recommender.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_visit_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserVisitReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 500)  // null 가능
    private String photoUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_locked", nullable = false)
    private boolean lock;  // 전시 기록 공개 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private UserVisit userVisit;
}