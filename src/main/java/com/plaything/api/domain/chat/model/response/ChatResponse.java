package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ChatResponse(
    @Schema(description = "채팅 list")
    List<Chat> list
) {

}
