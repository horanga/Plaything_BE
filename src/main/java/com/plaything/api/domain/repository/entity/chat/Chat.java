package com.plaything.api.domain.repository.entity.chat;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String sender;

    @Column
    private String receiver;

    @Column
    private String message;

    @Column
    private Timestamp createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;
}
