package com.plaything.api.domain.auth.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "유저 생성 response")
public record CreateUserResponse(
        @Schema(description = "성공 유무")
        String code
) {}
