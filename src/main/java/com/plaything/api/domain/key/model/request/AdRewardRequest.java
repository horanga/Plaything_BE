package com.plaything.api.domain.key.model.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "광고 리워드 제공")
public record AdRewardRequest(

    @Schema(description = "광고 유형")
    @NotBlank
    String adType,

    @Schema(description = "광고 시청 시간")
    @Positive
    @NotNull
    int viewDuration
) {

}