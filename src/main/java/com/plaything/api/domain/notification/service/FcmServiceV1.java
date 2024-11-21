package com.plaything.api.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.plaything.api.domain.notification.model.response.NotificationMessage.MATCHING_REQUEST_BODY;
import static com.plaything.api.domain.notification.model.response.NotificationMessage.MATCHING_REQUEST_TITLE;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmServiceV1 {

    private final String MATCHING_NOTIFICATION = "님이 매칭 요청을 보냈습니다.";

    public void sendMessageTo(
            Profile requesterProfile,
            ProfileImageResponse requesterMainPhoto,
            String fcmToken) {

        Message message = makeMessage(requesterProfile, requesterMainPhoto, fcmToken);

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Message makeMessage(Profile requesterProfile,
                                ProfileImageResponse requesterMainPhoto,
                                String fckToken) {

        return Message.builder().putData("title", requesterProfile.getNickName() + MATCHING_REQUEST_TITLE)
                .putData("requesterNickname", requesterProfile.getNickName())
                .putData("profileImage", requesterMainPhoto.url())
                .putData("body", MATCHING_REQUEST_BODY)
                .setToken(fckToken)
                .build();
    }
}
