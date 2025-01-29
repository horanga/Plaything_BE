package com.plaything.api.domain.matching.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserMatchingResponse(
        @Schema(description = "매칭 조회 list")
        List<UserMatching> list
) {
}
