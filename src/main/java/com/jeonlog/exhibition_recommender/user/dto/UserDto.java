package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
public class UserDto {
    private final Long id;
    private final String name;
    private final String email;
    private final Gender gender;
    private final Integer birthYear;
    private final OauthProvider oauthProvider;
    private final String introduction;
    private final String profileImageUrl;
    private final String nickname;
    private final int followerCount;
    private final int followingCount;
    private final String signature;

    @Builder
    private UserDto(Long id, String name, String email, Gender gender, Integer birthYear,
                    OauthProvider oauthProvider, String introduction, String profileImageUrl, String nickname, int followerCount, int followingCount, String signature) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthYear = birthYear;
        this.oauthProvider = oauthProvider;
        this.introduction = introduction;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.signature = signature;
    }
    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthYear(user.getBirthYear())
                .oauthProvider(user.getOauthProvider())
                .introduction(user.getIntroduction())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .signature(user.getSignature())
                .build();
    }

    @Getter
    @Builder
    public static class UserSearchResponse {
        private Long userId;
        private String nickname;
        private String name;
        private String profileImageUrl;
        private String introduction;
        private String signature;
        private int followerCount;
        private int followingCount;

        public static UserSearchResponse of(User user) {
            return UserSearchResponse.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .name(user.getName())
                    .profileImageUrl(user.getProfileImageUrl())
                    .introduction(user.getIntroduction())
                    .signature(user.getSignature())
                    .followerCount(user.getFollowerCount())
                    .followingCount(user.getFollowingCount())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetailResponse {
        private Long userId;
        private String name;
        private String email;
        private Gender gender;
        private Integer birthYear;
        private String introduction;
        private String profileImageUrl;
        private String nickname;
        private Integer followerCount;
        private Integer followingCount;
        private String signature;

        public static UserDetailResponse from(User user) {
            return UserDetailResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .gender(user.getGender())
                    .birthYear(user.getBirthYear())
                    .introduction(user.getIntroduction())
                    .profileImageUrl(user.getProfileImageUrl())
                    .nickname(user.getNickname())
                    .followerCount(user.getFollowerCount())
                    .followingCount(user.getFollowingCount())
                    .signature(user.getSignature())
                    .build();
        }
    }



}
