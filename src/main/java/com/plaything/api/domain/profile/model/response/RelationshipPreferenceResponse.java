package com.plaything.api.domain.profile.model.response;

import com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.repository.entity.profile.RelationshipPreference;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User의 선호하는 관계")
public record RelationshipPreferenceResponse(
    @Schema(description = "선호 관계")
    RelationshipPreferenceConstant relationshipPreference
) {

  public static RelationshipPreferenceResponse toResponse(
      RelationshipPreference relationshipPreference) {
    return new RelationshipPreferenceResponse(relationshipPreference.getRelationshipPreference());
  }
}
