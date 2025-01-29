package com.plaything.api.domain.notification.model.response;


import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_BODY;
import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_TITLE;
import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;

import com.plaything.api.domain.notification.constant.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림")
public record Notification(

    @Schema(description = "알림 id")
    Long id,

    @Schema(description = "알림 타입")
    NotificationType type,

    @Schema(description = "매칭 요청자 로그인 id")
    String requesterLoginId,

    @Schema(description = "매칭 요청자 닉네임")
    String requesterNickName,

    @Schema(description = "매칭 요청자의 대표 사진")
    String requesterMainPhoto,

    @Schema(description = "알림 제목")
    String title,

    @Schema(description = "알림 내용")
    String body) {

  public static Notification toResponse(
      com.plaything.api.domain.repository.entity.notification.Notification notification) {

    if (notification.getType().equals(MATCHING_REQUEST)) {
      return new Notification(
          notification.getId(),
          notification.getType(),
          notification.getRequesterLoginId(),
          notification.getRequesterNickName(),
          notification.getRequesterMainPhoto(),
          notification.getRequesterNickName() + MATCHING_REQUEST_TITLE,
          MATCHING_REQUEST_BODY);
    } else {
      return new Notification(
          notification.getId(),
          notification.getType(),
          notification.getRequesterLoginId(),
          notification.getRequesterNickName(),
          notification.getRequesterMainPhoto(),
          notification.getRequesterNickName() + MATCHING_REQUEST_TITLE,
          MATCHING_REQUEST_BODY);
    }
  }
}