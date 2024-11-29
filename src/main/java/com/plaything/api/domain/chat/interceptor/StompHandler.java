package com.plaything.api.domain.chat.interceptor;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.user.model.response.ProfileResponse;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {


    private static final Logger log = LoggerFactory.getLogger(StompHandler.class);
    private final Map<String, String[]> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, String[]> matchingMap = new ConcurrentHashMap<>();
    private final ProfileFacadeV1 profileFacadeV1;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (validateSend(accessor)) {
            return message;
        }

        if (validateDisconnect(accessor)) {
            return message;
        }

        if (validateConnect(accessor)) {
            return message;
        }

        if (validateSubscribe(accessor)) {
            return message;
        }
        return message;
    }

    private boolean validateSend(StompHeaderAccessor accessor) {
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String token = JWTProvider.extractToken(accessor.getFirstNativeHeader("Authorization"));
            String user = JWTProvider.getUserFromToken(token);

            String loginId = sessionUserMap.get(accessor.getSessionId())[1];//로그인 id

            if (!user.equals(loginId)) {
                throw new CustomException(ErrorCode.NOT_AUTHORIZED_USER);
            }

            if (!matchingMap.containsKey(loginId)) {

            }

            if (!isMatchingPartner(loginId, accessor)) {
                throw new CustomException(ErrorCode.NOT_MATCHING_PARTNER);
            }

            return true;

        }
        return false;

    }


    private boolean validateConnect(StompHeaderAccessor accessor) {
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = JWTProvider.extractToken(accessor.getFirstNativeHeader("Authorization"));
            String user = JWTProvider.getUserFromToken(token);
            ProfileResponse profileByLoginId = profileFacadeV1.getProfileByLoginId(user);
            String[] userInfo = new String[]{profileByLoginId.nickName(), user}; //[0] 닉네임, [1] 로그인 id
            sessionUserMap.put(accessor.getSessionId(), userInfo);

            return true;
        }
        return false;
    }


    private boolean validateDisconnect(StompHeaderAccessor accessor) {
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // 연결 종료 시 정보 삭제
            sessionUserMap.remove(accessor.getSessionId());
            return true;
        }
        return false;
    }

    private boolean validateSubscribe(StompHeaderAccessor accessor) {
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String sessionId = accessor.getSessionId();
            String user = sessionUserMap.get(sessionId)[0];
            String requestedUser = extractUserFromDestination(accessor.getDestination());

            if (!user.equals(requestedUser)) {
                throw new CustomException(ErrorCode.NOT_AUTHORIZED_SUBSCRIBE);
            }

            return true;
        }

        return false;
    }


    private boolean isMatchingPartner(String loginId, StompHeaderAccessor accessor) {
        String[] strings = matchingMap.get(loginId);
        return Arrays.stream(strings).anyMatch(i -> i.equals(extractUserFromDestination(accessor.getDestination())));

    }

    private String extractUserFromDestination(String destination) {
        String[] parts = destination.split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        throw new IllegalArgumentException("Invalid destination format");
    }

    @Scheduled(cron = "0 0 5 * * *")
    private void cleanupMap() {

        sessionUserMap.clear();
        matchingMap.clear();
        log.info("stomp 데이터 삭제");

    }
}
