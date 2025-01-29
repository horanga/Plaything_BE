package com.plaything.api.domain.chat.model.response;

import java.util.List;

public record SentChatResponse(
        List<ChatWithMissingChat> list
) {
}
