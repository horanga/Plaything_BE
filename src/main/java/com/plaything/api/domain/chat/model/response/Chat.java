package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;


public record Chat(
    @Schema(description = "메시지 id")
    Long id,

    @Schema(description = "메시지 발송자 로그인 id")
    String senderLoginId,

    @Schema(description = "메시지 내용")
    String message,

    @Schema(description = "메시지 생성 시간")
    LocalDateTime createdAt,

    @Schema(description = "메시지 순서")
    int sequence
) {

  public static Chat toResponse(com.plaything.api.domain.repository.entity.chat.Chat chat,
      String loginId) {

    if (loginId.equals(chat.getSenderLoginId())) {
      return new Chat(
          chat.getId(),
          chat.getSenderLoginId(),
          chat.getMessage(),
          chat.getCreatedAt(),
          chat.getSequence());
    }

    return new Chat(
        chat.getId(),
        chat.getSenderLoginId(),
        chat.getMessage(),
        chat.getCreatedAt(),
        chat.getSequence());
  }
}

