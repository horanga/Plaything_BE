package com.plaything.api.domain.notification.service;

import com.plaything.api.domain.notification.constant.NotificationType;
import com.plaything.api.domain.notification.model.response.Notification;
import com.plaything.api.domain.profile.util.ImageUrlGenerator;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationServiceV1 {

    private final ImageUrlGenerator urlGenerator;

    private final NotificationRepository notificationRepository;

    public void saveNotification(
            NotificationType type,
            String loginId,
            Profile requesterProfile,
            User receiver
    ) throws IOException {
        com.plaything.api.domain.repository.entity.notification.Notification notification = com.plaything.api.domain.repository.entity.notification.Notification.builder()
                .type(type)
                .requesterNickName(requesterProfile.getNickName())
                .requesterLoginId(loginId)
                .requesterMainPhoto(urlGenerator.getImageUrl(requesterProfile.getMainPhotoFileName()))
                .receiver(receiver)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getNotification(String receiver) {
        return notificationRepository.findByReceiver_LoginId(receiver).stream()
                .map(Notification::toResponse)
                .toList();
    }
}
