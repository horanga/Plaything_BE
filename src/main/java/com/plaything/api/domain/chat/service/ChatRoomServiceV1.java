package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.query.ChatRoomQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceV1 {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomQueryRepository chatRoomQueryRepository;


    public List<ChatRoom> findChatRooms(String requestNickname, Long lastChatRoomId) {
        //상대방이 탈퇴한 경우
        return chatRoomQueryRepository.findChatRooms(requestNickname, lastChatRoomId);
    }

    @Transactional
    public void leaveRoom(Long id, String requestNickName) {

        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_CHATROOM));

        if (!chatRoom.validateRequester(requestNickName)) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_CHAT_ROOM_USER);
        }
        chatRoom.leaveChatRoom(requestNickName);
    }

}
