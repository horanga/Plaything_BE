package com.plaything.api.domain.notification.controller;

import com.plaything.api.domain.notification.model.response.NotificationResponse;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notifications", description = "V1 Notification API")
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
    public ResponseEntity<List<NotificationResponse>> getNotification(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String user = userDetails.getUsername();
        return ResponseEntity.ok().body(notificationServiceV1.getNotification(user));
    }
}
