package com.jeonlog.exhibition_recommender.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String path = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH");
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT_PATH is not set");
        }

        try (FileInputStream in = new FileInputStream(path)) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(in)
                    .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            }
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }
}
