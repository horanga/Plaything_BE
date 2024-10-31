package com.plaything.api.domain.chat.service;

import com.plaything.api.domain.chat.model.Message;
import com.plaything.api.domain.chat.model.response.ChatListResponse;
import com.plaything.api.domain.repository.chat.ChatRepository;
import com.plaything.api.domain.repository.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.entity.chat.Chat;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatServiceV1 {

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;

    public ChatListResponse chatList(String from, String to) {

        //TODO 예외처리
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByUsers(from, to).orElseThrow();

        List<Chat> chats = chatRepository.findTop10ByChatRoomOrderByIdDesc(chatRoom);
        List<Message> res = chats.stream()
                .map(chat -> new Message(chat.getReceiver(), chat.getSender(), chat.getMessage()))
                .toList();

        return new ChatListResponse(res);
    }

    //웹소켓은 트랜잭션 경계가 명확하지 않다. 하나의 커넥션으로만 처리돼서
    @Transactional(transactionManager = "createChatTransactionManger")
    public void saveChatMessage(Message msg) {

        Optional<ChatRoom> chatRoomByUsers = chatRoomRepository.findChatRoomByUsers(msg.getFrom(), msg.getTo());

        if (chatRoomByUsers.isEmpty()) {

            //TODO 예외처리
            ChatRoom chatRoom = ChatRoom.builder()
                    .user1(msg.getFrom())
                    .user2(msg.getTo())
                    .build();

            chatRoomRepository.save(chatRoom);

            Chat chat = chat(msg, chatRoom);
            chatRepository.save(chat);

        }

        Chat chat = chat(msg, chatRoomByUsers.get());
        chatRepository.save(chat);
    }

    private Chat chat(Message msg, ChatRoom chatRoom) {
        return Chat.builder()
                .sender(msg.getFrom())
                .receiver(msg.getTo())
                .message(msg.getMessage())
                .chatRoom(chatRoom)
                .createAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }
}
