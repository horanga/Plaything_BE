package com.plaything.api.domain.repository.entity.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = {
        @Index(name = "idx_chatroom_sequence", columnList = "chatRoom_id, sequence"),
        @Index(name = "idx_chatroom_createdat", columnList = "chatRoom_id, createdAt")
})
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderLoginId;

    @Column(nullable = false)
    private String receiverLoginId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

}
