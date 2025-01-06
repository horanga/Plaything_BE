package com.plaything.api.domain.profile.model.response;

import com.plaything.api.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User 검색 리스트")
public record UserSearchResponse(
        @Schema(description = "error code")
        ErrorCode description,

        @Schema(description = "이름")
        List<String> name
) {}