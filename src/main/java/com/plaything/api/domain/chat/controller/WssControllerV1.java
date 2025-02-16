package com.plaything.api.domain.chat.controller;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorResponse;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.chat.model.response.SentChatResponse;
import com.plaything.api.domain.chat.service.ChatFacadeV1;
import com.plaything.api.security.JWTProvider;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
@Slf4j
public class WssControllerV1 {

  private final ChatFacadeV1 chatFacadeV1;
  private final SimpMessagingTemplate messagingTemplate;
  private final JWTProvider jwtProvider;

  @MessageMapping("/chat/list/{to}")
  public SentChatResponse sendMessage(@DestinationVariable String to, ChatRequest msg) {
    ChatWithMissingChat chatWithMissingChat = chatFacadeV1.saveMessage(msg, LocalDateTime.now());
    messagingTemplate.convertAndSendToUser(to, "/chat", chatWithMissingChat.chat());
    return new SentChatResponse(List.of(chatWithMissingChat));
  }

  @MessageExceptionHandler(CustomException.class)
  public void handleRuntimeException(CustomException ex) {
    messagingTemplate.convertAndSend(
        "/user/queue/errors",
        ErrorResponse.toResponse(ex)
    );
  }
}
