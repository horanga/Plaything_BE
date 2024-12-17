package com.plaything.api.domain.chat.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.handler.MessageBatchHandler;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.matching.model.response.MatchingResponse;
import com.plaything.api.domain.matching.service.MatchingServiceV1;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.user.service.UserServiceV1;
import com.plaything.api.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class SessionValidator {

    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, String> fcmTokenMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> matchingMap = new ConcurrentHashMap<>();
    private final MatchingServiceV1 matchingServiceV1;
    private final UserServiceV1 userServiceV1;
    private final MessageBatchHandler messageBatchHandler;
    private final ObjectMapper objectMapper;

    public void validateSend(String authHeader, String sessionId, String destination, Message<?> message) {

        String loginIdByHeader = getLoginId(authHeader).getLoginId();
        if (!sessionUserMap.containsKey(sessionId)) {
            throw new CustomException(ErrorCode.NOT_CONNECTED_STOMP);
        }
        String requesterLonginId = sessionUserMap.get(sessionId);

        if (!loginIdByHeader.equals(requesterLonginId)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_USER);
        }

        if (!matchingMap.containsKey(requesterLonginId)) {
            List<MatchingResponse> matchingResponse = matchingServiceV1.getMatchingResponse(requesterLonginId);
            List<String> matchingPartner = getMatchingPartner(requesterLonginId, matchingResponse);
            matchingMap.put(requesterLonginId, matchingPartner);
        }

        if (!isMatchingPartner(requesterLonginId, destination)) {
            throw new CustomException(ErrorCode.NOT_MATCHING_PARTNER);
        }

        String partnerLoginId = extractLoginIdFromDestination(destination);

        if (!sessionUserMap.containsKey(partnerLoginId)) {
            messageBatchHandler.queueMessage(requesterLonginId, parseMessage(message), fcmTokenMap.get(partnerLoginId));
        }

    }

    public void validateConnect(String authHeader, String sessionId) {
        if (sessionUserMap.containsKey(sessionId)) {
            throw new CustomException(ErrorCode.CONNECTION_ALREADY_EXIST);
        }

        User user = getLoginId(authHeader);
        sessionUserMap.put(sessionId, user.getLoginId());
        fcmTokenMap.put(sessionId, user.getFcmToken());
    }

    public void processDisconnect(String sessionId) {

        // 연결 종료 시 정보 삭제
        if (sessionUserMap.containsKey(sessionId)) {
            String loginId = sessionUserMap.get(sessionId);//닉네임;
            sessionUserMap.remove(sessionId);

            if (matchingMap.containsKey(loginId)) {
                matchingMap.remove(loginId);
            }
        }
    }

    public void validateSubscribe(String sessionId, String destination) {

        if (!sessionUserMap.containsKey(sessionId)) {
            throw new CustomException(ErrorCode.NOT_CONNECTED_STOMP);
        }
        String loginId = sessionUserMap.get(sessionId);
        String requestedUserLoginId = extractChannelFromDestinationForSubscribe(destination);

        //자기 자신의 채널만 구독 가능
        if (!loginId.equals(requestedUserLoginId)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_SUBSCRIBE);
        }
    }

    private boolean isMatchingPartner(String loginId, String destination) {
        List<String> matchingList = matchingMap.get(loginId);
        return matchingList.stream()
                .anyMatch(partner -> partner.equals(extractLoginIdFromDestination(destination)));
    }

    private String extractLoginIdFromDestination(String destination) {
        String[] parts = destination.split("/");
        if (parts[1].equals("pub")) {
            return parts[4];
        }

        throw new CustomException(ErrorCode.NOT_EXIST_USER);
    }

    private String extractChannelFromDestinationForSubscribe(String destination) {
        String[] parts = destination.split("/");

        if (parts.length >= 3) {
            return parts[2];
        }
        throw new CustomException(ErrorCode.NOT_AUTHORIZED_SUBSCRIBE);
    }

    private List<String> getMatchingPartner(String loginId, List<MatchingResponse> matchingResponses) {

        if (matchingResponses.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_EXIST_MATCHING_PARTNER);
        }

        MatchingResponse matchingResponse = matchingResponses.get(0);

        if (matchingResponse.senderLoginId().equals(loginId)) {
            return matchingResponses.stream().map(MatchingResponse::receiverLoginId).toList();
        }

        return matchingResponses.stream().map(MatchingResponse::senderLoginId).toList();
    }

    private User getLoginId(String authHeader) {

        String token = JWTProvider.extractToken(authHeader);
        String user = JWTProvider.getUserFromToken(token);

        return userServiceV1.findByLoginId(user);
    }

    public void clean() {
        sessionUserMap.clear();
        matchingMap.clear();
    }

    @Scheduled(cron = "0 0 5 * * *")
    private void cleanupMap() {

        sessionUserMap.clear();
        matchingMap.clear();
        fcmTokenMap.clear();
        log.info("stomp 데이터 삭제");
    }

    private ChatRequest parseMessage(Message<?> message) {
        ChatRequest chatRequest = null;

        try {
            // payload가 byte[] 형태로 들어있을 수 있음
            Object payload = message.getPayload();
            if (payload instanceof byte[]) {
                String jsonString = new String((byte[]) payload);
                chatRequest = objectMapper.readValue(jsonString, ChatRequest.class);
            }
            // 또는 이미 String 형태일 수 있음
            else if (payload instanceof String) {
                chatRequest = objectMapper.readValue((String) payload, ChatRequest.class);
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return chatRequest;
    }
}
