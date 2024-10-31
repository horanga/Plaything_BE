package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.user.constants.Gender;
import com.plaything.api.domain.user.constants.PrimaryRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "User 프로필")
public record ProfileResponse(
        @Schema(description = "프로필 비공개")
        boolean isPrivate,

        @Schema(description = "유저 사진")
        List<ProfileImageResponse> profileImageResponses,

        @Schema(description = "닉네임")
        String nickName,

        @Schema(description = "자기 소개")
        String introduction,

        @Schema(description = "성별")
        Gender gender,

        @Schema(description = "대표 성향")
        PrimaryRole primaryRole,

        @Schema(description = "생년월일")
        LocalDate birthDate,

        @Schema(description = "세부 성향")
        List<PersonalityTraitResponse> personalityTrait,

        @Schema(description = "선호 관계")
        List<RelationshipPreferenceResponse> relationshipPreference
) {

    public static ProfileResponse toResponse(Profile profile, List<ProfileImageResponse> profileImages){

        List<PersonalityTraitResponse> personalityTraitList
                = profile.getPersonalityTrait().stream()
                .map(PersonalityTraitResponse::toResponse)
                .toList();

        List<RelationshipPreferenceResponse> relationshipPreferenceList = profile.getRelationshipPreference().stream()
                .map(RelationshipPreferenceResponse::toResponse)
                .toList();

        return new ProfileResponse(
                profile.isPrivate(),
                profileImages,
                profile.getNickName(),
                profile.getIntroduction(),
                profile.getGender(),
                profile.getPrimaryRole(),
                profile.getBirthDate(),
                personalityTraitList,
                relationshipPreferenceList);
    }
}

