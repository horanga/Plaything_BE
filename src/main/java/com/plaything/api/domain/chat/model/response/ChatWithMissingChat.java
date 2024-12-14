package com.plaything.api.domain.chat.model.response;

import com.plaything.api.domain.chat.model.reqeust.ChatWithSequence;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "실시간 채팅 응답")
public record ChatWithMissingChat(
        @Schema(description = "전송된 메시지")
        ChatWithSequence chat,

        @Schema(description = "누락됐던 메시지")
        List<ChatWithSequence> missingChat
) {
}
