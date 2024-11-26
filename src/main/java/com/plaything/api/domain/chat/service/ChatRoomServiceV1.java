package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceV1 {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom findChatRoom(Long chatRoomId){

       return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_CHATROOM));
    }

    @Transactional
    public void leaveRoom(Long id, String requestNickName){

        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_CHATROOM));

        if(!chatRoom.validateRequester(requestNickName)){
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_CHAT_ROOM_USER);
        }
        chatRoom.leaveChatRoom(requestNickName);
    }

}
