package com.plaything.api.domain.chat.model.response;

import com.plaything.api.domain.repository.entity.chat.Chat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;


public record MessageResponse(
        @Schema(description = "메시지 id")
        Long id,

        @Schema(description = "메시지 발송자 닉네임")
        String sendNickname,

        @Schema(description = "메시지 내용")
        String message,

        @Schema(description = "메시지 생성 시간")
        LocalDateTime createdAt
) {

    public static MessageResponse toResponse(Chat chat, String nickName) {


        if (nickName.equals(chat.getSenderNickname())) {
            return new MessageResponse(
                    chat.getId(),
                    chat.getSenderNickname(),
                    chat.getMessage(),
                    chat.getCreatedAt());
        }

        return new MessageResponse(
                chat.getId(),
                chat.getSenderNickname(),
                chat.getMessage(),
                chat.getCreatedAt());
    }
}

