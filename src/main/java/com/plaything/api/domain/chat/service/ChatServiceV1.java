package com.plaything.api.domain.chat.service;

import com.plaything.api.domain.chat.model.reqeust.Message;
import com.plaything.api.domain.chat.model.response.ChatListResponse;
import com.plaything.api.domain.chat.model.response.MessageResponse;
import com.plaything.api.domain.repository.entity.chat.Chat;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.repo.chat.ChatRepository;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.query.ChatQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatServiceV1 {

    private final ChatRateLimiter chatRateLimiter;

    private final ChatQueryRepository chatQueryRepository;

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;


    public ChatListResponse chatList(Long chatRoomId, String requestNickName, Long lastId, LocalDate now) {

        List<Chat> chats = chatQueryRepository.findChat(chatRoomId, lastId, now);
        List<MessageResponse> res = chats.stream()
                .map(i -> MessageResponse.toResponse(i, requestNickName))
                .toList();
        return new ChatListResponse(res);
    }

    //웹소켓은 트랜잭션 경계가 명확하지 않다. 하나의 커넥션으로만 처리돼서
    @Transactional(transactionManager = "createChatTransactionManger")
    public Chat saveChatMessage(Message msg, LocalDateTime now) {
        chatRateLimiter.checkRate(msg.senderNickname());
        Optional<ChatRoom> chatRoomByUsers = chatRoomRepository.findChatRoomByUsers(msg.senderNickname(), msg.receiverNickname());

        if (chatRoomByUsers.isEmpty()) {
            //TODO 예외처리
            ChatRoom chatRoom = ChatRoom.builder()
                    .senderNickname(msg.senderNickname())
                    .receiverNickname(msg.receiverNickname())
                    .build();
            chatRoom.updateLastMessage(msg.senderNickname(), msg.message(), LocalDateTime.now());
            Chat chat = chat(msg, chatRoom, now);
            chatRoomRepository.save(chatRoom);
            return chatRepository.save(chat);
        }
        Chat chat = chat(msg, chatRoomByUsers.get(), now);
        chatRoomByUsers.get().updateLastMessage(msg.senderNickname(), msg.message(), LocalDateTime.now());
        return chatRepository.save(chat);
    }

    private Chat chat(Message msg, ChatRoom chatRoom, LocalDateTime now) {
        return Chat.builder()
                .senderNickname(msg.senderNickname())
                .receiverNickname(msg.receiverNickname())
                .message(msg.message())
                .chatRoom(chatRoom)
                .createdAt(now)
                .build();
    }
}