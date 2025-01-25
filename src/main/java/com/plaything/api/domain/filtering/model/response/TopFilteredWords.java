package com.plaything.api.domain.filtering.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "필터링 통계")
public record TopFilteredWords(
        @Schema(description = "필터링된 단어")
        String word,
        @Schema(description = "누적 횟수")
        int count) {
}
