package com.plaything.api.domain.key.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "매칭 요청")
public record MatchingRequest(

    @Schema(description = "매칭 상대방 로그인 ID")
    @NotBlank
    String partnerLoginId
) {

}