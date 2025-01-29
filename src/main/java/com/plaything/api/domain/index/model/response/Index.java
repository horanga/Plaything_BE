package com.plaything.api.domain.index.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인덱스 api 응답")
public record Index(

    @Schema(description = "새로운 메시지 도착 여부")
    boolean hasNewChat) {

}
