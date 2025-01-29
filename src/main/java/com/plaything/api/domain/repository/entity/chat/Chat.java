package com.plaything.api.domain.repository.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
