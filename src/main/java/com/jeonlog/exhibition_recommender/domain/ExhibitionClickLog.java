package com.jeonlog.exhibition_recommender.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "exhibition_click_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  //외부에서 생성 방지
@AllArgsConstructor
@Builder
public class ExhibitionClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate clickedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Exhibition exhibition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

}