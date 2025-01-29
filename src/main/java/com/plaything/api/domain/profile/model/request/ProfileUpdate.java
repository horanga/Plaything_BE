package com.plaything.api.domain.profile.model.request;


import com.plaything.api.domain.profile.constants.Gender;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

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

    @Schema(description = "제거할 상세 성향의 List 인덱스")
    @NotNull
    List<Integer> personalityTraitsToRemove,

    @Schema(description = "새로 넣을 상세 성향")
    @NotNull
    List<PersonalityTraitConstant> personalityTraitConstant,

    @Schema(description = "대표 상세 성향")
    PersonalityTraitConstant primaryTrait,

    @Schema(description = "제거할 선호 관계의 List 인덱스")
    @NotNull
    List<Integer> relationshipPreferenceConstantToRemove,

    @Schema(description = "선호하는 관계")
    @NotNull
    List<RelationshipPreferenceConstant> relationshipPreferenceConstant
) {

}
