package com.jeonlog.exhibition_recommender.auth.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleLoginRequestDto {
    private String authorizationCode;
}
