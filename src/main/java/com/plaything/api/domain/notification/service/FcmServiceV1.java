package com.plaything.api.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.plaything.api.domain.chat.model.response.ChatProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmServiceV1 {

    public void sendMessageTo(
            ChatProfile chatProfile,
            String title,
            String message,
            String fcmToken) {

        Message msg = makeMessage(chatProfile, title, message, fcmToken);

        try {
            FirebaseMessaging.getInstance().send(msg);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Message makeMessage(ChatProfile chatProfile,
                                String title,
                                String message,
                                String fckToken) {

        return Message.builder().putData("title", title)
                .putData("partner", chatProfile.nickName())
                .putData("partnerImage", chatProfile.mainPhoto())
                .putData("body", message)
                .setToken(fckToken)
                .build();
    }
}
