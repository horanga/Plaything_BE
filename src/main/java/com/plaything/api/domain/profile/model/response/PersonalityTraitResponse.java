package com.plaything.api.domain.profile.model.response;

import com.plaything.api.domain.repository.entity.profile.PersonalityTrait;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User의 세부 성향")
public record PersonalityTraitResponse(
        @Schema(description = "세부 성향")
        PersonalityTraitConstant personalityTrait,

        @Schema(description = "대표 세부 성향 여부")
        boolean isPrimaryTrait
) {
    public static PersonalityTraitResponse toResponse(PersonalityTrait personalityTrait){
        return new PersonalityTraitResponse(
                personalityTrait.getTrait(),
                personalityTrait.isPrimaryTrait());
    }
}
