package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRoomResponse(

        @Schema(description ="채팅 내역")
        ChatListResponse chatList,

        @Schema(description ="상대방 프로필")
        ChatProfile chatProfile
) {}
