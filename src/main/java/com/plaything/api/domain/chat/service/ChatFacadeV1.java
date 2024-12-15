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
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.util.ImageUrlGenerator;
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

    public ChatList getChatList(String requesterLoginId, Long chatRoomId, Long lastChatId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        String requestNickname = user.getNickname();
        chatRoomServiceV1.checkChatRomm(chatRoomId, requestNickname);
        return chatServiceV1.chatList(chatRoomId, requestNickname, lastChatId, LocalDate.now());
    }

    public ChatWithMissingChat saveMessage(ChatRequest chatRequest, LocalDateTime now) {
        filteringService.filterWords(chatRequest.chat());
        chatRateLimiter.checkRate(chatRequest.senderNickname());
        return chatServiceV1.saveChatMessage(chatRequest, now);
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
                    i.getLastChat(),
                    i.getLastChatAt(),
                    i.getLastChatSender(),
                    ChatProfile.toResponse(profile, urlGenerator.getImageUrl(profile.getMainPhotoFileName())));
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
