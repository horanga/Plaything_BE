package com.plaything.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WssConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //pub, sub 경로 지정

        //응답을 내려주는 경로
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins("http://localhost:3000",
                        "https://fe-chat.vercel.app",
                        "https://fe-chat-jeongs-projects-496987bc.vercel.app");
// 클라이언트가 웹 소켓을 사용할 수 없는 환경에서 방어 로직 -->프록시나 방화벽으로 차단될 때
// 롱폴링을 대신 사용하게 됨

    }

}
