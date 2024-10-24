package com.plaything.api.domain.repository;

import com.plaything.api.domain.repository.entity.Chat;
import com.plaything.api.domain.repository.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findTop10ByChatRoomOrderByIdDesc(ChatRoom chatRoom);

//    @Query("SELECT c FROM Chat AS c WHERE c.sender =:sender OR c.receiver =:receiver ORDER BY c.id DESC LIMIT 10")
//    List<Chat> findTop10Chats(@Param("sender") String sender, @Param("receiver") String receiver);
}
