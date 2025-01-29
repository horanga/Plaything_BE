package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ChatRoom(

    @Schema(description = "채팅방 id")
    Long chatRoomId,

    @Schema(description = "최신 채팅 메시지")
    String lastChat,

    @Schema(description = "최신 채팅 메시지 시간")
    LocalDateTime lastChatAt,

    @Schema(description = "최신 채팅 보낸 사람")
    String lastChatSender,

    @Schema(description = "파트너 프로필")
    ChatProfile partnerProfile
) {

}
