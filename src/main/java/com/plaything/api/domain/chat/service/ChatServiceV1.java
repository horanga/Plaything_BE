package com.plaything.api.domain.chat.service;

import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.reqeust.ChatWithSequence;
import com.plaything.api.domain.chat.model.response.ChatList;
import com.plaything.api.domain.chat.model.response.ChatResponse;
import com.plaything.api.domain.chat.model.response.ChatWithMissingChat;
import com.plaything.api.domain.repository.entity.chat.Chat;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.repo.chat.ChatRepository;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.query.ChatQueryRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceV1 {

    private final ChatQueryRepository chatQueryRepository;

    private final ChatRepository chatRepository;

    private final ChatRoomRepository chatRoomRepository;


    public ChatList chatList(Long chatRoomId, String requestNickName, Long lastId, LocalDate now) {

        List<Chat> chats = chatQueryRepository.findChat(chatRoomId, lastId, now);
        List<ChatResponse> res = chats.stream()
                .map(i -> ChatResponse.toResponse(i, requestNickName))
                .toList();
        return new ChatList(res);
    }

    //웹소켓은 트랜잭션 경계가 명확하지 않다. 하나의 커넥션으로만 처리돼서
    @Retryable(retryFor = OptimisticLockException.class, backoff = @Backoff(delay = 500), maxAttempts = 2)
    @Transactional(transactionManager = "createChatTransactionManger", noRollbackFor = Exception.class)
    public ChatWithMissingChat saveChatMessage(ChatRequest msg, LocalDateTime now) {
        Optional<ChatRoom> chatRoomByUsers = chatRoomRepository.findChatRoomByUsers(msg.senderNickname(), msg.receiverNickname());

        if (chatRoomByUsers.isEmpty()) {
            //TODO 예외처리
            ChatRoom chatRoom = ChatRoom.builder()
                    .senderNickname(msg.senderNickname())
                    .receiverNickname(msg.receiverNickname())
                    .build();
            chatRoom.updateLastMessage(msg.senderNickname(), msg.chat(), LocalDateTime.now(), 1);
            Chat chat = chat(msg, chatRoom, now, 1);
            chatRoomRepository.save(chatRoom);
            chatRepository.save(chat);
            return new ChatWithMissingChat(ChatWithSequence.from(msg, 1, now), Collections.emptyList());
        }

        ChatRoom room = chatRoomByUsers.get();
        int newSequence = room.getLastSequence() + 1;
        Chat chat = chat(msg, chatRoomByUsers.get(), now, newSequence);
        chatRepository.save(chat);
        room.updateLastMessage(msg.senderNickname(), msg.chat(), LocalDateTime.now(), newSequence);

        List<ChatWithSequence> missingList = getMissingChats(msg, room);
        return new ChatWithMissingChat(ChatWithSequence.from(msg, newSequence, now), missingList);
    }

    private List<ChatWithSequence> getMissingChats(ChatRequest msg, ChatRoom room) {
        List<Chat> missingChat = new ArrayList<>();

        if (msg.lastChatSequence() < room.getLastSequence()) {
            List<Integer> missingSequences = IntStream.range(msg.lastChatSequence() + 1, room.getLastSequence())
                    .mapToObj(Integer::valueOf)
                    .toList();

            missingChat.addAll(chatRepository.findMissingChat(room.getId(), missingSequences));
        }

        if (msg.lastChatSequence() > room.getLastSequence()) {
            log.warn("Sequence mismatch detected - Client sequence({}) is greater than room sequence({}). RoomId: {}, Sender: {}",
                    msg.lastChatSequence(),
                    room.getLastSequence(),
                    room.getId(),
                    msg.senderNickname()
            );

        }

        List<ChatWithSequence> missingList = missingChat.stream().map(ChatWithSequence::toResponse).toList();
        return missingList;
    }


    private Chat chat(ChatRequest msg, ChatRoom chatRoom, LocalDateTime now, int sequence) {
        return Chat.builder()
                .senderNickname(msg.senderNickname())
                .receiverNickname(msg.receiverNickname())
                .message(msg.chat())
                .chatRoom(chatRoom)
                .createdAt(now)
                .sequence(sequence)
                .build();
    }
}