package com.plaything.api.domain.chat.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Chatting List")
public record ChatList(
    @Schema(description = "return Message : []")
    List<Chat> list
) {

}
