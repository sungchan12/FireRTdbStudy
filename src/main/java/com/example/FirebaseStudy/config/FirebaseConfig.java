package com.example.FirebaseStudy.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseDatabase firebaseDatabase() throws IOException {
        // 서비스 계정 키를 가져옵니다 (resources 폴더에 위치)
        ClassPathResource serviceAccount = new ClassPathResource("");

        // Firebase 옵션 설정
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                .setDatabaseUrl("https://~")
                .build();
        FirebaseApp.initializeApp(options);
        return FirebaseDatabase.getInstance();
    }
}