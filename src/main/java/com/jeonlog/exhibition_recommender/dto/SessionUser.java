package com.jeonlog.exhibition_recommender.dto;

import com.jeonlog.exhibition_recommender.domain.user.User;
import lombok.Getter;

@Getter
public class SessionUser {

    private final String name;
    private final String email;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
