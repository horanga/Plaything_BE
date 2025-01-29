package com.plaything.api.domain.matching.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매칭 관련 데이터")
public record MatchingData(

    @Schema(description = "매칭 신청한 사람 목록")
    List<String> candidateList,
    @Schema(description = "매칭된 목록")
    List<String> matchingList,
    @Schema(description = "숨김처리한 프로필")
    List<String> hideList,
    @Schema(description = "마지막으로 본 프로필 id")
    String lastProfileId,
    @Schema(description = "프로필 skip 횟수")
    String count,
    @Schema(description = "Redis 상태")
    boolean isAvailable) {

}
