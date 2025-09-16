package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {
    private final String name;
    private final String email;
    private final Gender gender;
    private final Integer birthYear;
    private final OauthProvider oauthProvider;
    private final String introduction;
    private final String profileImageUrl;
    private final String nickname;

    @Builder
    private UserDto(String name, String email, Gender gender, Integer birthYear,
                   OauthProvider oauthProvider, String introduction, String profileImageUrl, String nickname) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthYear = birthYear;
        this.oauthProvider = oauthProvider;
        this.introduction = introduction;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
    }

    public static UserDto from(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthYear(user.getBirthYear())
                .oauthProvider(user.getOauthProvider())
                .introduction(user.getIntroduction())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .build();
    }
}