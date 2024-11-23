package com.plaything.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {

            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            FileInputStream serviceAccount = new FileInputStream(resource.getFile());
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
