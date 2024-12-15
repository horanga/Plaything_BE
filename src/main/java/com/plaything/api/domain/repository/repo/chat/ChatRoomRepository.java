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
            SELECT c FROM ChatRoom c WHERE (c.senderNickname = :user1 AND c.receiverNickname = :user2)
            OR (c.senderNickname = :user2 AND c.receiverNickname = :user1) AND c.isClosed is false
            """)
    Optional<ChatRoom> findChatRoomByUsers(
            @Param("user1") String requestNickname,
            @Param("user2") String partnerNickname
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

