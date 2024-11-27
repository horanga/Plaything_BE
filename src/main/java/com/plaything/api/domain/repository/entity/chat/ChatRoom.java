package com.plaything.api.domain.repository.entity.chat;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.model.reqeust.Message;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_receiver_last_msg", columnList = "receiverNickname,lastChatMessageAt"),
        @Index(name = "idx_sender_last_msg", columnList = "senderNickname,lastChatMessageAt")
})
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String senderNickname;

    @Column
    private String receiverNickname;

    @Column
    private String exitedUserNickname;

    @Column
    private String lastChatMessage;

    @Column
    private LocalDateTime lastChatMessageAt;

    @Column
    private boolean isClosed = false;


    public boolean validateRequester(String name) {
        return senderNickname.equals(name) || receiverNickname.equals(name);
    }

    public void hasPartnerLeave() {
        if (!this.exitedUserNickname.isEmpty()) {
            throw new CustomException(ErrorCode.PARTNER_ALREADY_LEAVE);
        }
    }

    public void isOver() {
        if (this.isClosed) {
            throw new CustomException(ErrorCode.CHAT_ROOM_IS_OVER);
        }
    }

    public void leaveChatRoom(String nickName) {
        if (this.exitedUserNickname == null) {
            this.exitedUserNickname = nickName;
        } else {
            isClosed = true;
        }
    }

    public void updateLastMessage(Message message, LocalDateTime createdAt) {
        this.lastChatMessage = message.message();
        this.lastChatMessageAt = createdAt;
    }

}
