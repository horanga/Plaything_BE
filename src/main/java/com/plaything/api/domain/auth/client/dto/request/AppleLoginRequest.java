package com.plaything.api.domain.auth.client.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "애플 Login 요청")
public record AppleLoginRequest(
        @Schema(description = "애플 OAuth2 identity 토큰")
        @NotBlank
        @NotNull
        String identityToken,

        @Schema(description = "디바이스 fcmToken")
        @NotBlank
        @NotNull
        String fcmToken
) {
}