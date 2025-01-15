package com.plaything.api.domain.chat.handler;

import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatProfile;
import com.plaything.api.domain.notification.service.FcmServiceV1;
import com.plaything.api.domain.profile.util.ImageUrlGenerator;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageBatchHandler {

    private static final int WAIT_TIME_SECONDS = 5;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Queue<ChatRequest>> messageQueues = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    private final FcmServiceV1 fcmService;

    @Autowired
    private final ProfileRepository profileRepository;

    @Autowired
    private final ImageUrlGenerator imageUrlGenerator;

    public void queueMessage(String userId, ChatRequest chatRequest, String fcmToken) {
        if (!scheduledTasks.containsKey(userId)) {
            scheduleMessageDelivery(userId, fcmToken);
        }
        Queue<ChatRequest> queue = messageQueues.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());

        queue.offer(chatRequest);
    }

    public void scheduleMessageDelivery(String userId, String fcmToken) {
        ScheduledFuture<?> task = scheduler.schedule(
                () -> sendFirebaseNotification(userId, fcmToken),
                WAIT_TIME_SECONDS,
                TimeUnit.SECONDS
        );
        scheduledTasks.put(userId, task);
    }

    public void sendFirebaseNotification(String userId, String fcmToken) {
        try {
            Queue<ChatRequest> queue = messageQueues.get(userId);
            if (queue == null || queue.isEmpty()) {
                return;
            }

            List<ChatRequest> messages = new ArrayList<>();
            ChatRequest message;
            while ((message = queue.poll()) != null) {
                messages.add(message);
            }

            Profile profile = profileRepository.findByLoginId(List.of(userId)).get(0);

            String imageUrl = imageUrlGenerator.getImageUrl(profile.getMainPhotoFileName());
            ChatProfile chatProfile = ChatProfile.toResponse(profile, imageUrl);

            String msg = makeMessage(messages);

            // FCM으로만 전송
//            fcmService.sendMessageTo(chatProfile, "새로운 메시지가 도착했습니다", msg, fcmToken);

        } catch (Exception e) {
            log.error("FCM 전송 중 에러 발생", e);
        } finally {
            scheduledTasks.remove(userId);
        }
    }

    private String makeMessage(List<ChatRequest> messages) {
        return messages.size() == 1 ? messages.get(0).chat() :
                messages.size() + "개의 메시지가 도착했습니다.";

    }
}
