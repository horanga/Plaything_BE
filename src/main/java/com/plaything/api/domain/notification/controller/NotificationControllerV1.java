package com.plaything.api.domain.notification.controller;

import com.plaything.api.domain.notification.model.response.NotificationResponse;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification API", description = "V1 Notification API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationControllerV1 {

    private final NotificationServiceV1 notificationServiceV1;

    @Operation(
            summary = "Get notification",
            description = "알림 목록 조회하기"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-notification")
    public List<NotificationResponse> getNotification(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return notificationServiceV1.getNotification(user);
    }
}
