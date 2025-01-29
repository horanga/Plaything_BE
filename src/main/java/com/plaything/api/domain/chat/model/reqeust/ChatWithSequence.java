package com.plaything.api.domain.chat.model.reqeust;

import com.plaything.api.domain.repository.entity.chat.Chat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "채팅 전송")
public record ChatWithSequence(

    @Schema(description = "요청자 로그인 id")
    String senderLoginId,

    @Schema(description = "수신자 로그인 id")
    String receiverLoginId,

    @Schema(description = "메시지 내용")
    String chat,

    @Schema(description = "메시지 순서")
    int sequence,

    @Schema(description = "메시지 전송 시간")
    LocalDateTime createdAt
) {

  public static ChatWithSequence from(ChatRequest chatRequest, int sequence, LocalDateTime time) {
    return new ChatWithSequence(
        chatRequest.senderLoginId(),
        chatRequest.receiverLoginId(),
        chatRequest.chat(),
        sequence,
        time);
  }

  public static ChatWithSequence toResponse(Chat chat) {
    return new ChatWithSequence(
        chat.getSenderLoginId(),
        chat.getReceiverLoginId(),
        chat.getMessage(),
        chat.getSequence(),
        chat.getCreatedAt());
  }
}

