package com.jeonlog.exhibition_recommender.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterPushTokenRequest {
    private Long userId;      // TODO: 나중엔 인증에서 꺼내고 제거
    private String token;     // ExponentPushToken[...]
    private String platform;  // EXPO
}
