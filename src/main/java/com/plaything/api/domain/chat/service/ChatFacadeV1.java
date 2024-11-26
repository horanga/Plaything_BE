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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ChatFacadeV1 {

    private final UserRepository userRepository;

    private final ChatRoomServiceV1 chatRoomServiceV1;

    private final ChatServiceV1 chatServiceV1;

    private final ProfileRepository profileRepository;

    private final FilteringService filteringService;

    public ChatRoomResponse getChatRoom(Long id, String requesterLoginId, LocalDate now) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        String requestNickname = user.getNickname();
        ChatRoom chatRoom = chatRoomServiceV1.findChatRoom(id);

        if (!chatRoom.validateRequester(requestNickname)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_CHAT_ROOM_USER);
        }

        ChatListResponse chatListResponse = chatServiceV1.chatList(requestNickname, chatRoom, null, now);
        Profile partnerProfile = profileRepository.findByNickName(chatRoom.getReceiverNickname());
        ChatProfile chatProfile = ChatProfile.toResponse(partnerProfile);
        return new ChatRoomResponse(chatListResponse, chatProfile);
    }

    public void leaveChatRoom(Long id, String requesterLoginId) {
        User user = userRepository.findByLoginId(requesterLoginId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_USER));
        String requestNickname = user.getNickname();
        chatRoomServiceV1.leaveRoom(id, requestNickname);
    }

    public ChatListResponse getChatList(String requesterLoginId, Long chatRoomId, Long lastChatId) {
        ChatRoom chatRoom = chatRoomServiceV1.findChatRoom(chatRoomId);
        return chatServiceV1.chatList(requesterLoginId, chatRoom, lastChatId, LocalDate.now());
    }


    public void saveMessage(Message message, LocalDateTime now) {
        filteringService.filterWords(message.message());
        chatServiceV1.saveChatMessage(message, now);

    }

}
