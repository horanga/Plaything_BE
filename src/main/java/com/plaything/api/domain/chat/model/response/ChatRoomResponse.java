package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ChatRoomResponse(
        @Schema(description = "채팅방 list")
        List<ChatRoom> chatRoomList
) {
}
