package com.jeonlog.exhibition_recommender.domain;

import com.jeonlog.exhibition_recommender.domain.exhibition.Exhibition;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exhibition_genres",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exhibition_id","genre_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExhibitionGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Exhibition exhibition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Genre genre;
}