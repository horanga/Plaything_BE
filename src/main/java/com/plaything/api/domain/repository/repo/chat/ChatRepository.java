package com.plaything.api.domain.repository.repo.chat;

import com.plaything.api.domain.repository.entity.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatRepository extends JpaRepository<Chat, Long> {

}
