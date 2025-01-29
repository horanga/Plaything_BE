package com.plaything.api.domain.matching.model.request;

import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이용자 매칭 요청")
public record MatchRequest(

    @Schema(description = "대표 성향 그룹")
    PrimaryRole primaryRole,

    @Schema(description = "상세 성향")
    PersonalityTraitConstant personalityTraitConstant,

    @Schema(description = "마지막으로 받은 유저의 id")
    long lastId,

    @Schema(description = "이용자 닉네임")
    String userName

) {

  public static MatchRequest from(
      PrimaryRole primaryRole,
      PersonalityTraitConstant partnerTrait,
      long lastId,
      String userName) {
    return new MatchRequest(primaryRole, partnerTrait, lastId, userName);
  }

}