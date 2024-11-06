package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.user.profile.RelationshipPreference;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.querydsl.core.Tuple;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.plaything.api.domain.repository.entity.user.profile.QProfile.profile;

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

    public static UserMatching toResponse(Tuple tuple) {

        return new UserMatching(
                tuple.get(profile.primaryRole),
                tuple.get(profile.nickName),
                tuple.get(profile.birthDate),
                tuple.get(profile.introduction),
                convertToPersonalityTraitResponses(tuple.get(profile.personalityTrait)),
                convertToRelationshipPreferenceResponses(tuple.get(profile.relationshipPreference)),
                convertToProfileImageResponses(tuple.get(profile.profileImages))
        );

    }

    private static List<PersonalityTraitResponse> convertToPersonalityTraitResponses(List<PersonalityTrait> traits) {
        return traits.stream()
                .map(PersonalityTraitResponse::toResponse)
                .collect(Collectors.toList());
    }

    private static List<RelationshipPreferenceResponse> convertToRelationshipPreferenceResponses(List<RelationshipPreference> preferences) {
        return preferences.stream()
                .map(RelationshipPreferenceResponse::toResponse)
                .collect(Collectors.toList());
    }

    private static List<ProfileImageResponse> convertToProfileImageResponses(List<ProfileImage> images) {
        return images.stream()
                .map(ProfileImageResponse::toResponse)
                .collect(Collectors.toList());
    }
}
