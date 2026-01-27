package com.jeonlog.exhibition_recommender.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FcmPushClient {

    public void send(String fcmToken, String title, String body, Map<String, Object> data) {
        if (fcmToken == null || fcmToken.isBlank()) return;

        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null) {
            for (Map.Entry<String, Object> e : data.entrySet()) {
                if (e.getValue() != null) {
                    builder.putData(e.getKey(), String.valueOf(e.getValue()));
                }
            }
        }

        try {
            FirebaseMessaging.getInstance().send(builder.build());
        } catch (Exception ignored) {
            // 토큰 만료/실패 처리하고 싶으면 여기서 pt 비활성화 로직 추가해도 됨
        }
    }
}
