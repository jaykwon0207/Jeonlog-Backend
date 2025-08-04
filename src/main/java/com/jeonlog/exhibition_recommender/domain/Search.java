package com.jeonlog.exhibition_recommender.domain;

import com.jeonlog.exhibition_recommender.domain.exhibition.Exhibition;
import com.jeonlog.exhibition_recommender.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "searches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Exhibition exhibition;  // null 가능

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
}