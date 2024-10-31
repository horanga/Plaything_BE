package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User의 세부 성향")
public record PersonalityTraitResponse(
        @Schema(description = "세부 성향")
        PersonalityTraitConstant personalityTrait
) {
    public static PersonalityTraitResponse toResponse(PersonalityTrait personalityTrait){
        return new PersonalityTraitResponse(personalityTrait.getPersonalityTrait());
    }
}
