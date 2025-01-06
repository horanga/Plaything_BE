package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatList;
import com.plaything.api.domain.chat.model.response.ChatProfile;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.filtering.service.FilteringService;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.profile.util.ImageUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatFacadeV1 {

    private static final Logger log = LoggerFactory.getLogger(ChatFacadeV1.class);
    private final UserRepository userRepository;

    private final ChatRoomServiceV1 chatRoomServiceV1;

    private final ChatServiceV1 chatServiceV1;

    private final ProfileRepository profileRepository;

    private final FilteringService filteringService;

    private final ChatRateLimiter chatRateLimiter;

    private final ImageUrlGenerator urlGenerator;

    public List<ChatRoomResponse> getChatRooms(String requesterLoginId, Long lastChatRoomId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));

        List<ChatRoom> chatRooms = chatRoomServiceV1.findChatRooms(requesterLoginId, lastChatRoomId);
        Map<String, Profile> profileMap = getProfileMap(chatRooms, requesterLoginId);

        return getChatRoomResponse(chatRooms, profileMap, requesterLoginId);
    }

    public void leaveChatRoom(Long id, String requesterLoginId) {
        chatRoomServiceV1.leaveRoom(id, requesterLoginId);
    }

    public ChatList getChatList(String requesterLoginId, Long chatRoomId, Long lastChatId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        chatRoomServiceV1.checkChatRoom(chatRoomId, user.getLoginId());
        return chatServiceV1.chatList(chatRoomId, user.getNickname(), lastChatId, LocalDate.now());
    }

    public ChatWithMissingChat saveMessage(ChatRequest chatRequest, LocalDateTime now) {
        filteringService.filterWords(chatRequest.chat());
        chatRateLimiter.checkRate(chatRequest.senderLoginId());
        return chatServiceV1.saveChatMessage(chatRequest, now);
    }


    @Scheduled(cron = "${schedules.cron.data.cleanup}")
    public void cleanupChatRateDate() {
        chatRateLimiter.cleanupOldData();
        log.info("채팅 rate 삭제:" + LocalDateTime.now());
    }

    private Map<String, Profile> getProfileMap(List<ChatRoom> chatRooms, String requestNickname) {
        List<String> partnerLoginId
                = chatRooms.stream().map(i -> getPartnerLoginId(i, requestNickname)).toList();

        List<Profile> profileList = profileRepository.findByLoginId(partnerLoginId);


        return profileList.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getLoginId(),
                        p -> p
                ));
    }

    private List<ChatRoomResponse> getChatRoomResponse(List<ChatRoom> chatRooms, Map<String, Profile> profileMap, String requestLoginId) {

        return chatRooms.stream()
                .map(chatRoom -> new ChatRoomResponse(
                        chatRoom.getId(),
                        chatRoom.getLastChat(),
                        chatRoom.getLastChatAt(),
                        chatRoom.getLastChatSender(),
                        ChatProfile.toResponse(
                                profileMap.get(getPartnerLoginId(chatRoom, requestLoginId)),
                                urlGenerator.getImageUrl(profileMap.get(getPartnerLoginId(chatRoom, requestLoginId)).getMainPhotoFileName())
                        )
                ))
                .toList();
    }

    private String getPartnerLoginId(ChatRoom chatRoom, String requesterNickname) {
        return chatRoom.getSenderLoginId().equals(requesterNickname) ?
                chatRoom.getReceiverLoginId()
                : chatRoom.getSenderLoginId();
    }
}
