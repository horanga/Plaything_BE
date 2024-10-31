package com.plaything.api.domain.user.model.request;


import com.plaything.api.domain.user.constants.Gender;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "유저 프로필 변경")
public record ProfileUpdate(

        @Schema(description = "닉네임")
        @NotBlank
        String nickName,

        @Schema(description = "성별")
        @NotNull
        Gender gender,

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
