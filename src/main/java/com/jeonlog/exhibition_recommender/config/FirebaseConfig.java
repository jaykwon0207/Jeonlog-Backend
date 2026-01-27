package com.jeonlog.exhibition_recommender.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {


    @Value("${firebase.credentials.path:}")
    private Resource serviceAccountLocation;

    @PostConstruct
    public void init() {
        // 이미 초기화 되어있으면 스킵
        if (FirebaseApp.getApps() != null && !FirebaseApp.getApps().isEmpty()) {
            return;
        }

        // 설정값 없으면(로컬 등) Firebase 기능 안 쓰는 걸로 보고 스킵
        try {
            if (serviceAccountLocation == null || !serviceAccountLocation.exists()) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        try (InputStream in = serviceAccountLocation.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            throw new IllegalStateException("Firebase 초기화 실패", e);
        }
    }
}
