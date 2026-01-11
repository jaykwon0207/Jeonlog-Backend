package com.jeonlog.exhibition_recommender.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TempOAuthDto {
    private String email;
    private String name;
    private String oauthProvider;
    private String oauthId;
}