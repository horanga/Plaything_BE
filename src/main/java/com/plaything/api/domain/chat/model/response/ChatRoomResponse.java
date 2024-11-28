package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatRoomResponse(

        @Schema(description = "채팅방 id")
        Long chatRoomId,

        @Schema(description = "최신 채팅 메시지")
        String lastChatMessage,

        @Schema(description = "최신 채팅 메시지")
        LocalDateTime lastMessageAt,

        @Schema(description = "상대방 프로필")
        ChatProfile chatProfile
) {
}
