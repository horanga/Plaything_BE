package com.plaything.api.domain.matching.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.service.ChatRoomServiceV1;
import com.plaything.api.domain.key.service.PointKeyServiceV1;
import com.plaything.api.domain.matching.model.response.MatchingResponse;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.user.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;

@RequiredArgsConstructor
@Service
public class MatchingServiceV1 {

    private final UserServiceV1 userServiceV1;
    private final MatchingRepository matchingRepository;
    private final PointKeyServiceV1 pointKeyServiceV1;
    private final NotificationServiceV1 notificationServiceV1;
    private final ChatRoomServiceV1 chatRoomServiceV1;

    public List<UserMatching> match(String user, long lastId) {
        return userServiceV1.searchPartner(user, lastId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void creatMatching(User requester, User partner, String transactionId) {
        pointKeyServiceV1.usePointKey(requester, partner, transactionId);
        createMatchingLog(requester.getLoginId(), partner.getLoginId());
        try {
            notificationServiceV1.saveNotification(
                    MATCHING_REQUEST,
                    requester.getProfile(),
                    partner);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.NOTIFICATION_SAVED_FAILED);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void acceptMatching(User matchingReceiver, User matchingSender, String transactionId) {
        pointKeyServiceV1.usePointKey(matchingReceiver, matchingSender, transactionId);
        Matching matching = matchingRepository.findBySenderLoginIdAndReceiverLoginId(
                        matchingSender.getLoginId(), matchingReceiver.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_MATCHING));
        matching.acceptMatching();
        chatRoomServiceV1.creatChatRoom(matchingSender.getLoginId(), matchingReceiver.getLoginId());
    }

    public void createMatchingLog(String senderLoginId, String receiverLoginId) {
        Matching matching = Matching.builder()
                .senderLoginId(senderLoginId)
                .receiverLoginId(receiverLoginId)
                .build();
        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchingResponse(String loginId) {
        return matchingRepository.findSuccessAndNotOveMatching(loginId).stream()
                .map(MatchingResponse::toResponse).toList();
    }
}
