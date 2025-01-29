package com.plaything.api.domain.auth.model.response;

import com.plaything.api.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Login 요청")
public record Login(
    @Schema(description = "error code")
    ErrorCode description,
    @Schema(description = "프로필 유무")
    boolean invalidProfile,
    @Schema(description = "일일 포인트 키 지급 여부")
    boolean dailyRewardProvided
) {

}
