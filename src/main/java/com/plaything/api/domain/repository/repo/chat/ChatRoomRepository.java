package com.plaything.api.domain.repository.repo.chat;

import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            SELECT c FROM ChatRoom c WHERE (c.senderLoginId = :user1 AND c.receiverLoginId = :user2)
            OR (c.senderLoginId = :user2 AND c.receiverLoginId = :user1) AND c.isClosed is false
            """)
    Optional<ChatRoom> findChatRoomByUsers(
            @Param("user1") String senderLoginId,
            @Param("user2") String receiverLoginId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ChatRoom cr set cr.lastSequence =:lastSequence, cr.lastChat =:lastChatAt, cr.lastChatSender =:lastSender where cr.id =:roodId")
    void updateSequence(
            @Param("lastSequence") int lastSequence,
            @Param("lastChatAt") LocalDateTime lastChatAt,
            @Param("lastSender") String lastChatSender,
            @Param("roodId") long roodId
    );

}

