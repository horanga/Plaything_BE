package com.plaything.api.domain.chat.model.response;

import com.plaything.api.domain.repository.entity.chat.Chat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;


public record ChatResponse(
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

    public static ChatResponse toResponse(Chat chat, String loginId) {

        if (loginId.equals(chat.getSenderLoginId())) {
            return new ChatResponse(
                    chat.getId(),
                    chat.getSenderLoginId(),
                    chat.getMessage(),
                    chat.getCreatedAt(),
                    chat.getSequence());
        }

        return new ChatResponse(
                chat.getId(),
                chat.getSenderLoginId(),
                chat.getMessage(),
                chat.getCreatedAt(),
                chat.getSequence());
    }
}

