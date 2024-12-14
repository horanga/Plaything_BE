package com.plaything.api.domain.repository.entity.chat;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
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
        @Index(name = "idx_receiver_msg", columnList = "receiverNickname,lastChatAt"),
        @Index(name = "idx_sender_msg", columnList = "senderNickname,lastChatAt")
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
    private String lastChat;

    @Column
    private LocalDateTime lastChatAt;

    @Column
    private boolean hasNewChat;

    @Column
    private LocalDateTime lastChatCheckedAt;

    @Column
    String lastChatSender;

    @Column
    private int lastSequence;

    @Column
    private boolean isClosed = false;

    @Version
    private Long version;

    public boolean validateRequester(String name) {
        return senderNickname.equals(name) || receiverNickname.equals(name);
    }

    public void hasPartnerLeave() {
        if (this.exitedUserNickname != null) {
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

    public void updateLastMessage(String sender, String message, LocalDateTime createdAt, int newSequence) {
        this.lastChat = message;
        this.lastChatAt = createdAt;
        this.lastChatSender = sender;
        this.lastSequence = newSequence;
        if (!this.hasNewChat) {
            this.hasNewChat = true;
        }
    }

    public void getMessages(String nickName) {
        if (this.hasNewChat && !this.lastChatSender.equals(nickName)) {
            this.hasNewChat = false;
        }
        this.lastChatCheckedAt = LocalDateTime.now();
    }
}
