package com.jeonlog.exhibition_recommender.recommendation.domain;


import com.jeonlog.exhibition_recommender.exhibition.domain.ExhibitionTheme;
import com.jeonlog.exhibition_recommender.exhibition.domain.GenreType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "initial_exhibitions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InitialExhibition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, unique = true)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 500)
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GenreType genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExhibitionTheme exhibitionTheme;

}
