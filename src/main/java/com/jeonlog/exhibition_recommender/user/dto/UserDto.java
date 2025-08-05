package com.jeonlog.exhibition_recommender.user.dto;

import com.jeonlog.exhibition_recommender.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private String email;
    private String name;

    public static UserDto from(User user) {
        return new UserDto(user.getEmail(), user.getName());
    }
}