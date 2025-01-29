package com.plaything.api.domain.matching.model.response;


import com.plaything.api.domain.repository.entity.matching.Matching;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매칭 정보")
public record MatchingResponse(
    @Schema(description = "요청자 닉네임")
    String senderLoginId,

    @Schema(description = "수신자 닉네임")
    String receiverLoginId) {

  public static MatchingResponse toResponse(Matching matching) {
    return new MatchingResponse(matching.getSenderLoginId(), matching.getReceiverLoginId());
  }
}

