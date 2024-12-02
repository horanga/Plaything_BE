package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.model.reqeust.Message;
import com.plaything.api.domain.chat.model.response.ChatListResponse;
import com.plaything.api.domain.chat.model.response.ChatProfile;
import com.plaything.api.domain.chat.model.response.ChatRoomResponse;
import com.plaything.api.domain.filtering.service.FilteringService;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
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

    public List<ChatRoomResponse> getChatRooms(String requesterLoginId, Long lastChatRoomId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        String requestNickname = user.getNickname();
        List<ChatRoom> chatRooms = chatRoomServiceV1.findChatRooms(requestNickname, lastChatRoomId);
        Map<String, Profile> profileMap = getProfileMap(chatRooms, requestNickname);

        return getChatRoomResponse(chatRooms, profileMap, requestNickname);
    }

    public void leaveChatRoom(Long id, String requesterLoginId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        String requestNickname = user.getNickname();
        chatRoomServiceV1.leaveRoom(id, requestNickname);
    }

    public ChatListResponse getChatList(String requesterLoginId, Long chatRoomId, Long lastChatId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        String requestNickname = user.getNickname();
        chatRoomServiceV1.checkChatRomm(chatRoomId, requestNickname);
        return chatServiceV1.chatList(chatRoomId, requestNickname, lastChatId, LocalDate.now());
    }

    public void saveMessage(Message message, LocalDateTime now) {
        filteringService.filterWords(message.message());
        chatServiceV1.saveChatMessage(message, now);
    }

    private List<ChatRoomResponse> getChatRoomResponse(List<ChatRoom> chatRooms, Map<String, Profile> profileMap, String requestNickName) {

        return chatRooms.stream().map(i -> {
            String name;
            if (i.getSenderNickname().equals(requestNickName)) {
                name = i.getReceiverNickname();
            } else {
                name = i.getSenderNickname();
            }
            Profile profile = profileMap.get(name);
            return new ChatRoomResponse(
                    i.getId(),
                    i.getLastChatMessage(),
                    i.getLastChatMessageAt(),
                    ChatProfile.toResponse(profile));
        }).toList();
    }

    public ChatRoom findByUsers(String senderNickName, String receiverNickname) {
        return chatRoomServiceV1.findByUsers(senderNickName, receiverNickname);
    }


    @Scheduled(cron = "${schedules.cron.data.cleanup}")
    public void cleanupChatRateDate() {
        chatRateLimiter.cleanupOldData();
        log.info("채팅 rate 삭제:" + LocalDateTime.now());
    }

    private Map<String, Profile> getProfileMap(List<ChatRoom> chatRooms, String requestNickname) {
        List<String> partnerNames
                = chatRooms.stream().map(i -> getPartnerNickname(i, requestNickname)).toList();
        List<Profile> profileList = profileRepository.findByNickNames(partnerNames);
        return profileList.stream()
                .collect(Collectors.toMap(
                        Profile::getNickName,
                        profile -> profile
                ));
    }

    private String getPartnerNickname(ChatRoom chatRoom, String requesterNickname) {
        return chatRoom.getSenderNickname().equals(requesterNickname) ?
                chatRoom.getReceiverNickname()
                : chatRoom.getSenderNickname();
    }
}
