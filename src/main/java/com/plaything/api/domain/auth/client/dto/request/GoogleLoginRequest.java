package com.plaything.api.domain.auth.client.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Login 요청")
public record GoogleLoginRequest(
    @Schema(description = "구글 OAuth2 액세스 토큰") @NotBlank @NotNull String accessToken,

    @Schema(description = "디바이스 fcmToken") @NotBlank @NotNull String fcmToken) {

}
