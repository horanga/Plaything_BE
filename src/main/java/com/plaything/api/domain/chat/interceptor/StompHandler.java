package com.plaything.api.domain.chat.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final SessionValidator sessionValidator;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        StompCommand command = accessor.getCommand();
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (command != null && command.equals(StompCommand.SEND)) {
            sessionValidator.validateSend(authHeader, sessionId, destination, message);
            return message;
        }

        if (command != null && command.equals(StompCommand.DISCONNECT)) {
            sessionValidator.processDisconnect(sessionId);
            return message;
        }

        if (command != null && command.equals(StompCommand.CONNECT)) {
            sessionValidator.validateConnect(authHeader, sessionId);
            return message;
        }

        if (command != null && command.equals(StompCommand.SUBSCRIBE)) {
            sessionValidator.validateSubscribe(sessionId, destination);
            return message;
        }

        return message;
    }

}
