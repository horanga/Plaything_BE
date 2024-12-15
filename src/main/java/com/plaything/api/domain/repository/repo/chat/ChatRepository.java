package com.plaything.api.domain.repository.repo.chat;

import com.plaything.api.domain.repository.entity.chat.Chat;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
            SELECT c FROM Chat c 
            WHERE c.chatRoom.id = :chatRoomId 
            AND c.sequence IN :sequences
            """)
    List<Chat> findMissingChat(
            @Param("chatRoomId") Long chatRoomId,
            @Param("sequences") List<Integer> sequences
    );

}
