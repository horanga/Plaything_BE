package com.plaything.api.domain.chat.model.reqeust;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "채팅 전송")
public record Message(

        @Schema(description = "요청자 닉네임")
        String senderNickname,

        @Schema(description = "수신자 닉네임")
        String receiverNickname,

        @Schema(description = "메시지 내용")
        String message
) {
}


