package com.jeonlog.exhibition_recommender.user.domain;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_visits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate visitedAt;

    @ManyToOne(fetch = FetchType.LAZY)  // 연관 객체 실제 접근 시 조회
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Exhibition exhibition;

}