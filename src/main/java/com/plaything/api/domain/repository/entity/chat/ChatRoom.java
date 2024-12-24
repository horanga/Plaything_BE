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
        @Index(name = "idx_receiver_msg", columnList = "receiverLoginId,lastChatAt"),
        @Index(name = "idx_sender_msg", columnList = "senderLoginId,lastChatAt")
})
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String senderLoginId;

    @Column
    private String receiverLoginId;

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
        return senderLoginId.equals(name) || receiverLoginId.equals(name);
    }

    public void hasPartnerLeftRoom() {
        if (this.exitedUserNickname != null) {
            throw new CustomException(ErrorCode.PARTNER_ALREADY_LEAVE);
        }
    }

    public void isChatRoomClosed() {
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

    public void checkAndClearNewMessageStatus(String loginId) {
        if (this.hasNewChat && !this.lastChatSender.equals(loginId)) {
            this.hasNewChat = false;
        }
        this.lastChatCheckedAt = LocalDateTime.now();
    }
}
