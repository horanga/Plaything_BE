package com.plaything.api.domain.notification.model.response;


import com.plaything.api.domain.notification.constant.NotificationType;
import com.plaything.api.domain.repository.entity.notification.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_BODY;
import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_TITLE;
import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;

@Schema(description = "알림")
public record NotificationResponse(

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
    public static NotificationResponse toResponse(Notification notification) {

        if (notification.getType().equals(MATCHING_REQUEST)) {
            return new NotificationResponse(
                    notification.getId(),
                    notification.getType(),
                    notification.getRequesterLoginId(),
                    notification.getRequesterNickName(),
                    notification.getRequesterMainPhoto(),
                    notification.getRequesterNickName() + MATCHING_REQUEST_TITLE,
                    MATCHING_REQUEST_BODY);
        } else {
            return new NotificationResponse(
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