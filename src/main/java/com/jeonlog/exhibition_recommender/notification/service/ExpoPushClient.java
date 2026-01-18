package com.jeonlog.exhibition_recommender.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExpoPushClient {

    private final WebClient webClient;

    public void send(String expoToken, String title, String body, Map<String, Object> data) {
        Map<String, Object> payload = Map.of(
                "to", expoToken,
                "title", title,
                "body", body,
                "data", data
        );

        // 실패 응답 처리(토큰 만료 등)는 NotificationService에서 deactivate 처리해도 됨
        webClient.post()
                .uri("https://exp.host/--/api/v2/push/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("") // 최소 처리
                .block();
    }
}
