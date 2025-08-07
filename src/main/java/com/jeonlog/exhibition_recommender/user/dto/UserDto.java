package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.Gender;
import com.jeonlog.exhibition_recommender.user.domain.OauthProvider;
import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {
    private final String name;
    private final String email;
    private final Gender gender;
    private final Integer birthYear;
    private final OauthProvider oauthProvider;

    @Builder
    public UserDto(String name, String email, Gender gender, Integer birthYear, OauthProvider oauthProvider) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.birthYear = birthYear;
        this.oauthProvider = oauthProvider;
    }

    public static UserDto from(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthYear(user.getBirthYear())
                .oauthProvider(user.getOauthProvider())
                .build();
    }
}