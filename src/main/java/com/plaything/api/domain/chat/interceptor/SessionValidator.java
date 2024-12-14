package com.plaything.api.domain.chat.interceptor;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.matching.model.response.MatchingResponse;
import com.plaything.api.domain.matching.service.MatchingServiceV1;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.UserServiceV1;
import com.plaything.api.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class SessionValidator {

    private final Map<String, String[]> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> matchingMap = new ConcurrentHashMap<>();
    private final ProfileFacadeV1 profileFacadeV1;
    private final MatchingServiceV1 matchingServiceV1;
    private final UserServiceV1 userServiceV1;

    public String validateSend(String authHeader, String sessionId, String destination) {

        String loginIdByHeader = getLoginId(authHeader);
        if (!sessionUserMap.containsKey(sessionId)) {
            throw new CustomException(ErrorCode.NOT_CONNECTED_STOMP);
        }
        String loginId = sessionUserMap.get(sessionId)[1];//로그인 id

        if (!loginIdByHeader.equals(loginId)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_USER);
        }

        String nickName = sessionUserMap.get(sessionId)[0];//닉네임;

        if (!matchingMap.containsKey(nickName)) {
            List<MatchingResponse> matchingResponse = matchingServiceV1.getMatchingResponse(nickName);
            List<String> list = getMatchingPartner(nickName, matchingResponse);
            matchingMap.put(nickName, list);
        }

        if (!isMatchingPartner(nickName, destination)) {
            throw new CustomException(ErrorCode.NOT_MATCHING_PARTNER);
        }

        return nickName;
    }

    public void validateConnect(String authHeader, String sessionId) {
        if (sessionUserMap.containsKey(sessionId)) {
            throw new CustomException(ErrorCode.CONNECTION_ALREADY_EXIST);
        }

        String loginId = getLoginId(authHeader);
        Profile profile = profileFacadeV1.getProfileByUserLoginId(loginId);
        String[] userInfo = new String[]{profile.getNickName(), loginId}; //[0] 닉네임, [1] 로그인 id
        sessionUserMap.put(sessionId, userInfo);
    }

    public void processDisconnect(String sessionId) {

        // 연결 종료 시 정보 삭제
        if (sessionUserMap.containsKey(sessionId)) {
            String nickName = sessionUserMap.get(sessionId)[0];//닉네임;
            sessionUserMap.remove(sessionId);

            if (matchingMap.containsKey(nickName)) {
                matchingMap.remove(nickName);
            }
        }
    }

    public void validateSubscribe(String sessionId, String destination) {

        if (!sessionUserMap.containsKey(sessionId)) {
            throw new CustomException(ErrorCode.NOT_CONNECTED_STOMP);
        }
        String user = sessionUserMap.get(sessionId)[0];
        String requestedUser = extractChannelFromDestination(destination);

        if (!user.equals(requestedUser)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_SUBSCRIBE);
        }
    }

    private boolean isMatchingPartner(String nickname, String destination) {
        List<String> strings = matchingMap.get(nickname);
        return strings.stream().anyMatch(i -> i.equals(extractReceiverFromDestination(destination)));
    }

    private String extractReceiverFromDestination(String destination) {
        String[] parts = destination.split("/");
        if (parts[1].equals("pub")) {
            return parts[4];
        }

        throw new CustomException(ErrorCode.NOT_EXIST_USER);
    }


    private String extractChannelFromDestination(String destination) {
        String[] parts = destination.split("/");

        if (parts.length >= 3) {
            return parts[2];
        }
        throw new CustomException(ErrorCode.NOT_AUTHORIZED_SUBSCRIBE);
    }

    private List<String> getMatchingPartner(String nickName, List<MatchingResponse> matchingResponses) {

        if (matchingResponses.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_EXIST_MATCHING_PARTNER);
        }

        MatchingResponse matchingResponse = matchingResponses.get(0);

        if (matchingResponse.senderNickname().equals(nickName)) {
            return matchingResponses.stream().map(MatchingResponse::receiverNickname).toList();
        }

        return matchingResponses.stream().map(MatchingResponse::senderNickname).toList();
    }

    private String getLoginId(String authHeader) {

        String token = JWTProvider.extractToken(authHeader);
        String user = JWTProvider.getUserFromToken(token);

        userServiceV1.findByLoginId(user);

        return user;
    }

    public void clean() {
        sessionUserMap.clear();
        matchingMap.clear();
    }

    @Scheduled(cron = "0 0 5 * * *")
    private void cleanupMap() {

        sessionUserMap.clear();
        matchingMap.clear();
        log.info("stomp 데이터 삭제");
    }
}
