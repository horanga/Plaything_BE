package com.plaything.api.domain.profile.model.request;


import com.plaything.api.domain.profile.constants.Gender;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "유저 프로필 변경")
public record ProfileUpdate(

        @Schema(description = "닉네임")
        @NotBlank
        String nickName,

        @Schema(description = "성별")
        @NotNull
        Gender gender,

        @Schema(description = "소개글")
        @Size(max = 30)
        @NotBlank
        String introduction,

        @Schema(description = "대표 성향")
        @NotNull
        PrimaryRole primaryRole,

        @Schema(description = "상세 성향")
        @NotNull
        PersonalityTraitConstant personalityTraitConstant,

        @Schema(description = "선호하는 관계")
        @NotNull
        RelationshipPreferenceConstant relationshipPreferenceConstant
) {}
