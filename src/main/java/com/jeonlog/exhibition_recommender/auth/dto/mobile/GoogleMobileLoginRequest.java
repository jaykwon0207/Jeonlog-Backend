package com.jeonlog.exhibition_recommender.auth.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleMobileLoginRequest {

    @NotBlank
    private String authorizationCode;

    @NotBlank
    private String codeVerifier;

    @NotBlank
    private String redirectUri;
}
