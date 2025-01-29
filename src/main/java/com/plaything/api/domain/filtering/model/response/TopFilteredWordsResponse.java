package com.plaything.api.domain.filtering.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "필터링 통계")
public record TopFilteredWordsResponse(
    @Schema(description = "필터링된 단어 list")
    List<TopFilteredWords> list) {

}