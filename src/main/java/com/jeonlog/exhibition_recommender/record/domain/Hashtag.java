package com.jeonlog.exhibition_recommender.record.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hashtags",
        uniqueConstraints = {
                // 해시태그 이름은 중복될 수 없도록 unique 제약조건 설정
                @UniqueConstraint(name = "UK_hashtag_name", columnNames = {"name"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 해시태그 이름

    // 서비스 레이어에서 사용할 생성자
    public Hashtag(String name) {
        this.name = name;
    }
}