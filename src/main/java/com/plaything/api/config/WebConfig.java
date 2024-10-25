package com.plaything.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**") // 모든 경로 허용
                .allowedOriginPatterns("http://localhost:*",
                        "https://fe-chat.vercel.app",
                        "https://fe-chat-jeongs-projects-496987bc.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true) // 자격 증명 허용
                .maxAge(3600); // preflight 요청 캐시 시간

    }
}
