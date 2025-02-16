package com.plaything.api.domain.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "User를 생성합니다.")
public record CreateUserRequest(
    @Schema(description = "로그인 id")
    @NotBlank
    @NotNull
    String loginId,

    @Schema(description = "유저 비밀번호")
    @NotBlank
    @NotNull
    String password,

    @Schema(description = "디바이스 fcmToken")
    @NotBlank
    @NotNull
    String fcmToken
) {

}
