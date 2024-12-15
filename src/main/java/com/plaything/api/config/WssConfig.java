package com.plaything.api.config;

import com.plaything.api.domain.chat.interceptor.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WssConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //pub, sub 경로 지정
        //응답을 내려주는 경로
        registry.enableSimpleBroker("/user")
                .setTaskScheduler(taskScheduler())
                .setHeartbeatValue(new long[]{10000, 10000});
        registry.setUserDestinationPrefix("/user");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins("http://localhost:3000",
                        "https://fe-chat.vercel.app",
                        "https://fe-chat-jeongs-projects-496987bc.vercel.app",
                        "https://jiangxy.github.io");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024)
                .setSendTimeLimit(2 * 10000)
                .setSendBufferSizeLimit(512 * 1024)
                .addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        super.afterConnectionEstablished(session);
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                        session.close(CloseStatus.SERVER_ERROR);
                        super.handleTransportError(session, exception);
                    }
                });
    }

}
