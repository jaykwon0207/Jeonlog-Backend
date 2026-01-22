package com.jeonlog.exhibition_recommender.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterPushTokenRequest {
    private String token;     //
    private String platform;  // "FCM" 같은 값으로 보내도 되고, 없어도 됨
}
