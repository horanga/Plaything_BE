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

    private final int START_OF_THE_CHAT_SEQUENCE = 0;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatRoomQueryRepository chatRoomQueryRepository;


    public List<ChatRoom> findChatRooms(String requestNickname, Long lastChatRoomId) {
        //상대방이 탈퇴한 경우
        return chatRoomQueryRepository.findChatRooms(requestNickname, lastChatRoomId);
    }

    public ChatRoom findByUsers(String senderNickname, String receiverNickname) {
        return chatRoomRepository.findChatRoomByUsers(senderNickname, receiverNickname)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_CHATROOM));
    }

    @Transactional
    public void checkChatRomm(Long chatRoomId, String nickName) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_CHATROOM));
        //검증을 이 순서대로 해야 종료된 채팅방이라는 메시지를 전달해줄 수 있다
        chatRoom.isOver();
        chatRoom.hasPartnerLeave();
        chatRoom.getMessages(nickName);
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

    public void creatChatRoom(String senderNickname, String receiverNickname) {
        ChatRoom chatRoom = ChatRoom.builder()
                .receiverNickname(senderNickname)
                .senderNickname(receiverNickname)
                .lastSequence(START_OF_THE_CHAT_SEQUENCE).build();
        chatRoomRepository.save(chatRoom);
    }


    public void hasNewChat() {

    }

}
