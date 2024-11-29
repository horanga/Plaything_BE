package com.plaything.api.domain.chat.controller;

import com.plaything.api.domain.chat.model.reqeust.Message;
import com.plaything.api.domain.chat.service.ChatFacadeV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
@Slf4j
public class WssControllerV1 {

    private final ChatFacadeV1 chatFacadeV1;

    @MessageMapping("/chat/message")
    @SendTo("/sub/chat")
    public Message receiveMessage(Message msg) {
        chatFacadeV1.saveMessage(msg, LocalDateTime.now());
        return msg;
    }
}
