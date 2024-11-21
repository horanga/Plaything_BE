package com.plaything.api.domain.notification.service;

import com.plaything.api.domain.notification.constant.NotificationType;
import com.plaything.api.domain.notification.model.response.NotificationResponse;
import com.plaything.api.domain.repository.entity.notification.Notification;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationServiceV1 {

    private final NotificationRepository notificationRepository;

    public void saveNotification(
            NotificationType type,
            Profile requesterProfile,
            User receiver
    ) throws IOException {
        Notification notification = Notification.builder()
                .type(type)
                .requesterNickName(requesterProfile.getNickName())
                .requesterMainPhoto(requesterProfile.getMainPhoto().getUrl())
                .receiver(receiver)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotification(String receiver) {
        return notificationRepository.findByReceiver_LoginId(receiver).stream()
                .map(NotificationResponse::toResponse)
                .toList();
    }
}
