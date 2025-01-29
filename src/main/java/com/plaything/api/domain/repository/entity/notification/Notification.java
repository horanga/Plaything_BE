package com.plaything.api.domain.repository.entity.notification;

import com.plaything.api.domain.notification.constant.NotificationType;
import com.plaything.api.domain.repository.entity.common.BaseEntity;
import com.plaything.api.domain.repository.entity.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Notification extends BaseEntity {

  //TODO 여기서 공지사항도 추가

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Enumerated(value = EnumType.STRING)
  private NotificationType type;

  private String requesterLoginId;

  private String requesterNickName;

  private String requesterMainPhoto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User receiver;
}
