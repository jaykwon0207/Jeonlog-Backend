package com.jeonlog.exhibition_recommender.like.dto;

import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LikeUserDto { // 전시에 좋아요 누른 사람들

    // 전시에 좋아요 누른 유저의 최소 프로필 정보
    private final Long id;
    private final String name;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final Integer birthYear;

    @Builder
    public LikeUserDto(Long id, String name, String email, String nickname, String profileImageUrl, Integer birthYear) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.birthYear = birthYear;
    }

    // User 엔티티 → DTO 변환
    public static LikeUserDto from(User u) {
        return LikeUserDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .nickname(u.getNickname())
                .profileImageUrl(u.getProfileImageUrl())
                .birthYear(u.getBirthYear())
                .build();
    }
}