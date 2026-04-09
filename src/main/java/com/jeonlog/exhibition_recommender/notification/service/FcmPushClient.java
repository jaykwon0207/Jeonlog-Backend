package com.jeonlog.exhibition_recommender.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmPushClient {

    public void send(String fcmToken, String title, String body, Map<String, Object> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("[PUSH] send_failed reason=blank_token");
            return;
        }

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
            log.warn(
                    "[PUSH] send_failed reason={} tokenLength={} tokenTail={}",
                    ignored.getMessage(),
                    fcmToken.length(),
                    maskTokenTail(fcmToken)
            );
        }
    }

    private String maskTokenTail(String token) {
        int length = token.length();
        int visibleLength = Math.min(6, length);
        String tail = token.substring(length - visibleLength);
        return "***" + tail;
    }
}
