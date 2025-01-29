package com.plaything.api.domain.auth.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Login 요청")
public record LoginRequest(
    @Schema(description = "로그인 ID")
    @NotBlank
    @NotNull
    String loginId,

    @Schema(description = "유저 비밀번호")
    @NotBlank
    @NotNull
    String password
) {

}
