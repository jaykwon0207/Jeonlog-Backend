package com.jeonlog.exhibition_recommender.domain.genre;

import com.jeonlog.exhibition_recommender.domain.user.Gender;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genres")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private GenreType genreType;
}