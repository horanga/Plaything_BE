package com.plaything.api.domain.user.model.request;

import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이용자 매칭 료청")
public record MatchRequest(

        @Schema(description = "상세 성향")
        PersonalityTraitConstant personalityTraitConstant,

        @Schema(description = "상세 성향")
        long lastId,

        @Schema(description = "이용자 로그인 id")
        String userName

) {

    public static MatchRequest from(
            PersonalityTraitConstant partnerTrait,
            long lastId,
            String userName) {
        return new MatchRequest(partnerTrait, lastId, userName);
    }

}