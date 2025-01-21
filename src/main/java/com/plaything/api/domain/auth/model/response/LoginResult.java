package com.plaything.api.domain.auth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResult(

        @Schema(description = "jwt 토큰")
        String token,
        @Schema(description = "로그인 결과")
        LoginResponse loginResponse) {
}
