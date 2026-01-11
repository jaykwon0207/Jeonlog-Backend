package com.jeonlog.exhibition_recommender.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TempOAuthDto {
    private String email;
    private String name;
    private String oauthProvider;
    private String oauthId;
}