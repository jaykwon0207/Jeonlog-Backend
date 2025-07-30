package com.jeonlog.exhibition_recommender.domain.user;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalDateTime;

@Entity
@Table (name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames ="email"),
        @UniqueConstraint (columnNames = "oauthId")})
@Getter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    //외부에서 생성 방지
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) // DB가 내부 식별용 ID 자동생성
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OauthProvider oauthProvider;

    @Column(nullable = false, length = 255)
    private String oauthId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    @Column(nullable = false)
    private Integer birthYear;

    public User update(String name){
        this.name = name;
        return this;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
