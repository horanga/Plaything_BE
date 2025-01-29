package com.plaything.api.domain.profile.model.response;

import com.plaything.api.domain.profile.constants.Gender;
import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.repository.entity.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "User 프로필")
public record MyPageProfile(

    @Schema(description = "프로필 id")
    Long id,

    @Schema(description = "프로필 비공개 여부")
    boolean isPrivate,

    @Schema(description = "프로필 밴 여부")
    boolean isBaned,

    @Schema(description = "프로필 상태")
    ProfileStatus profileStatus,

    @Schema(description = "유저 사진")
    List<ProfileImageResponse> profileImageList,

    @Schema(description = "닉네임")
    String nickName,

    @Schema(description = "자기 소개")
    String introduction,

    @Schema(description = "성별")
    Gender gender,

    @Schema(description = "대표 성향")
    String primaryRole,

    @Schema(description = "나이")
    int age,

    @Schema(description = "세부 성향")
    List<PersonalityTraitResponse> personalityTraitList,

    @Schema(description = "선호 관계")
    List<RelationshipPreferenceResponse> relationshipPreferenceList,

    @Schema(description = "마지막 광소 시청 시간")
    LocalDateTime lastAdViewTime,

    @Schema(description = "보유한 key 개수")
    long countOfKey

) {

  public static MyPageProfile toResponse(
      Profile profile,
      List<ProfileImageResponse> profileImages,
      LocalDateTime lastAdViewTime,
      long countOfKey) {

    List<PersonalityTraitResponse> personalityTraitList
        = profile.getPersonalityTrait().stream()
        .map(PersonalityTraitResponse::toResponse)
        .toList();

    List<RelationshipPreferenceResponse> relationshipPreferenceList = profile.getRelationshipPreference()
        .stream()
        .map(RelationshipPreferenceResponse::toResponse)
        .toList();

    String primaryRole = profile.getPrimaryRoleAsString();

    return new MyPageProfile(
        profile.getId(),
        profile.isPrivate(),
        profile.isBaned(),
        profile.getProfileStatus(),
        profileImages,
        profile.getNickName(),
        profile.getIntroduction(),
        profile.getGender(),
        primaryRole,
        profile.calculateAge(),
        personalityTraitList,
        relationshipPreferenceList,
        lastAdViewTime,
        countOfKey);
  }
}

