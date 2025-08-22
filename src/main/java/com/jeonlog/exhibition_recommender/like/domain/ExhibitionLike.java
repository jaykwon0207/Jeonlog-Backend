package com.jeonlog.exhibition_recommender.like.domain;

import com.jeonlog.exhibition_recommender.exhibition.domain.Exhibition;
import com.jeonlog.exhibition_recommender.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exhibition_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exhibition_like_user_exhibition",
                columnNames = {"user_id", "exhibition_id"}
        ),
        indexes = {
                @Index(name = "idx_exhibition_like_exhibition", columnList = "exhibition_id"),
                @Index(name = "idx_exhibition_like_user", columnList = "user_id")
        })
public class ExhibitionLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public ExhibitionLike(User user, Exhibition exhibition) {
        this.user = user;
        this.exhibition = exhibition;
    }
}