package com.plaything.api.domain.key.model.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용가능한 PoinKey")
public record AvailablePointKey(

    @Schema(description = "사용 가능한 포인트 키 개수")
    Long availablePointKey) {

}
