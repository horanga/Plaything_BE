package com.plaything.api.domain.chat.controller;

import com.plaything.api.domain.chat.model.reqeust.Message;
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
    public void receiveMessage(@DestinationVariable String to, Message msg) {
        // nativeHeaders에서 Authorization 헤더 추출
        chatFacadeV1.saveMessage(msg, LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(to, "/chat", msg);
    }
}
