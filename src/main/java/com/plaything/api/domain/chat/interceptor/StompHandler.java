package com.plaything.api.domain.chat.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
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

    private final ObjectMapper objectMapper;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        StompCommand command = accessor.getCommand();
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (command != null && command.equals(StompCommand.SEND)) {
            sessionValidator.validateSend(authHeader, sessionId, destination, parseMessage(message));
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

    private ChatRequest parseMessage(Message<?> message) {
        ChatRequest chatRequest = null;

        try {
            // payload가 byte[] 형태로 들어있을 수 있음
            Object payload = message.getPayload();
            if (payload instanceof byte[]) {
                String jsonString = new String((byte[]) payload);
                chatRequest = objectMapper.readValue(jsonString, ChatRequest.class);
            }
            // 또는 이미 String 형태일 수 있음
            else if (payload instanceof String) {
                chatRequest = objectMapper.readValue((String) payload, ChatRequest.class);
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return chatRequest;
    }

}
