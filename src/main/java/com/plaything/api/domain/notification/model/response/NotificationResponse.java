package com.plaything.api.domain.notification.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NotificationResponse(
        @Schema(description = "알림 list")
        List<Notification> list
) {
}
