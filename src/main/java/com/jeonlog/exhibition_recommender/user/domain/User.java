package com.jeonlog.exhibition_recommender.user.domain;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "oauth_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OauthProvider oauthProvider;

    @Column(nullable = false, unique = true)
    private String oauthId;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private Integer birthYear;

    @Column(length = 1000)
    private String introduction;

    @Column
    private String profileImageUrl;

    @Column(length = 20)
    private String signature;

    @Formula("(SELECT COUNT(*) FROM follow f WHERE f.following_id = id)")
    private int followerCount;

    @Formula("(SELECT COUNT(*) FROM follow f WHERE f.follower_id = id)")
    private int followingCount;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.signature == null) this.signature = "jeonlog";
    }

    public User update(String name) {
        this.name = name;
        return this;
    }

    public void updateSignature(String signature) {
        this.signature = signature;
    }

    public void updateProfile(Gender gender, Integer birthYear, String nickname,
                              String introduction, String profileImageUrl, String signature) {
        if (gender != null) this.gender = gender;
        if (birthYear != null) this.birthYear = birthYear;
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (introduction != null) this.introduction = introduction;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (signature != null && !signature.isBlank()) this.signature = signature;
    }
}