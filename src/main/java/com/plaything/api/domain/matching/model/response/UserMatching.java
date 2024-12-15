package com.plaything.api.domain.matching.model.response;

import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.model.response.PersonalityTraitResponse;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import com.plaything.api.domain.user.model.response.RelationshipPreferenceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "User 매칭")
public record UserMatching(
        @Schema(description = "대표 성향")
        PrimaryRole primaryRole,

        @Schema(description = "닉네임")
        String nickName,

        @Schema(description = "생년월일")
        LocalDate birthDate,

        @Schema(description = "자기 소개")
        String introduction,

        @Schema(description = "성향")
        List<PersonalityTraitResponse> personalityTraitList,

        @Schema(description = "추구 관계")
        List<RelationshipPreferenceResponse> relationshipPreferenceList,

        @Schema(description = "프로필 사진")
        List<ProfileImageResponse> profileList
) {

}
