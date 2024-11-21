package com.plaything.api.domain.key.model.response;


import com.plaything.api.domain.repository.entity.log.KeyUsageLog;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Key 사용 로그")
public record KeyUsageLogResponse(

        @Schema(description = "로그 id")
        Long id,

        @Schema(description = "키 사용인 id")
        long senderId,

        @Schema(description = "키 수신인 id")
        long receiverId

) {
    public static KeyUsageLogResponse toResponse(KeyUsageLog log) {
        return new KeyUsageLogResponse(log.getId(), log.getSenderId(), log.getReceiverId());
    }
}