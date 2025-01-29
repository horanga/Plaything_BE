package com.plaything.api.domain.key.model.response;

import com.plaything.api.domain.profile.model.response.UserResponse;
import com.plaything.api.domain.repository.entity.log.AdViewLog;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "광고 시청 이력")
public record AdViewLogResponse(

    @Schema(description = "로그 id")
    Long id,

    @Schema(description = "광고 타입")
    String adType,

    @Schema(description = "광고 시청 시간")
    int viewDuration,

    @Schema(description = "user 정보")
    UserResponse userResponse
) {

  public static AdViewLogResponse toResponse(AdViewLog log) {
    return new AdViewLogResponse(
        log.getId(),
        log.getAdType(),
        log.getViewDuration(),
        UserResponse.toResponse(log.getUser()));
  }
}