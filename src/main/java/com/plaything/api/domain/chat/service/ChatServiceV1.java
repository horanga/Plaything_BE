package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.reqeust.ChatWithSequence;
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

    public List<ChatResponse> chatList(Long chatRoomId, String requesterNickName, Long lastId, LocalDate now) {

        List<Chat> chats = chatQueryRepository.findChat(chatRoomId, lastId, now);
        return chats.stream()
                .map(i -> ChatResponse.toResponse(i, requesterNickName))
                .toList();
    }

    //웹소켓은 트랜잭션 경계가 명확하지 않다. 하나의 커넥션으로만 처리돼서
    @Retryable(retryFor = OptimisticLockException.class, backoff = @Backoff(delay = 500), maxAttempts = 2)
    @Transactional(transactionManager = "createChatTransactionManger", noRollbackFor = Exception.class)
    public ChatWithMissingChat saveChatMessage(ChatRequest msg, LocalDateTime now) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findChatRoomByUsers(msg.senderLoginId(), msg.receiverLoginId());
        ChatRoom room = chatRoom
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_MATCHING));

        int newSequence = room.getLastSequence() + 1;
        Chat chat = chat(msg, chatRoom.get(), now, newSequence);
        chatRepository.save(chat);

        room.updateLastMessage(msg.senderLoginId(), msg.chat(), LocalDateTime.now(), newSequence);
        List<ChatWithSequence> missingList = getMissingChats(msg, room);

        return new ChatWithMissingChat(ChatWithSequence.from(msg, newSequence, now), missingList);
    }

    private List<ChatWithSequence> getMissingChats(ChatRequest msg, ChatRoom room) {
        List<Chat> missingChat = new ArrayList<>();

        if (msg.lastChatSequence() < room.getLastSequence()) {
            List<Integer> missingSequences = IntStream.range(msg.lastChatSequence() + 1, room.getLastSequence())
                    .boxed()
                    .toList();

            missingChat.addAll(chatRepository.findMissingChat(room.getId(), missingSequences));
        }

        if (msg.lastChatSequence() > room.getLastSequence()) {
            log.warn("Sequence mismatch detected - Client sequence({}) is greater than room sequence({}). RoomId: {}, Sender: {}",
                    msg.lastChatSequence(),
                    room.getLastSequence(),
                    room.getId(),
                    msg.senderLoginId()
            );

        }
        return missingChat.stream().map(ChatWithSequence::toResponse).toList();
    }


    private Chat chat(ChatRequest msg, ChatRoom chatRoom, LocalDateTime now, int sequence) {
        return Chat.builder()
                .senderLoginId(msg.senderLoginId())
                .receiverLoginId(msg.receiverLoginId())
                .message(msg.chat())
                .chatRoom(chatRoom)
                .createdAt(now)
                .sequence(sequence)
                .build();
    }
}