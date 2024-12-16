package com.plaything.api.domain.chat.controller;

import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.chat.service.ChatFacadeV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
@Slf4j
public class WssControllerV1 {

    private final ChatFacadeV1 chatFacadeV1;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message/{to}")
    public ChatWithMissingChat sendMessage(@DestinationVariable String to, ChatRequest msg) {
        ChatWithMissingChat chatWithMissingChat = chatFacadeV1.saveMessage(msg, LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(to, "/chat", chatWithMissingChat.chat());
        return chatWithMissingChat;
    }
}
