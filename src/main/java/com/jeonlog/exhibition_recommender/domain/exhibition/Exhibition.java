package com.jeonlog.exhibition_recommender.domain.exhibition;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exhibitions")
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

    @Lob  //TEXT 타입 매핑
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 500)
    private String posterUrl;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(precision = 10, scale = 7)  // 전체 자릿수(정수부+소수부) 10, 소수점 아래 최대 7자리
    private BigDecimal latitude;   // 위도

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;  // 경도

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean isFree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExhibitionMood exhibitionMood;
}