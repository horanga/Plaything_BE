package com.plaything.api.domain.profile.service.profile;

import static com.plaything.api.domain.profile.constants.Constants.LIMIT_OF_SIZE_PERSONALITY_TRAIT;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.admin.service.ProfileMonitoringServiceV1;
import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.service.UserServiceV1;
import com.plaything.api.domain.repository.entity.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.profile.ProfileHidePreference;
import com.plaything.api.domain.repository.entity.profile.RelationshipPreference;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.profile.ProfileHidePreferenceRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProfileCommandServiceV1 {

  private final UserServiceV1 userServiceV1;
  private final ProfileRepository profileRepository;
  private final ProfileMonitoringServiceV1 profileMonitoringServiceV1;
  private final ProfileHidePreferenceRepository profileHidePreferenceRepository;


  public void registerProfile(ProfileRegistration registration, User user) {
    //TODO 대표성향을 한개만, 상세 성향은 6개까지만, 선호하는 관계는 모든 선택 가능
    //TODO 중복 안되도록 변경

    try {
      Profile profile = makesProfile(registration, user);
      user.setProfile(profile);
      profileRepository.save(profile);
      profileMonitoringServiceV1.saveProfileRecord(
          profile.getIntroduction(),
          profile.getNickName(),
          profile.getId(),
          profile.getProfileStatus(),
          user);
    } catch (CustomException e) {
      throw new CustomException(ErrorCode.TRAITS_NOT_INCLUDE_PRIMARY);
    }
  }

  @Transactional
  public void updateProfile(ProfileUpdate updateRequest, Profile profile) {

    List<PersonalityTrait> personalityTraitList =
        updateRequest.personalityTraitConstant().stream()
            .map(i -> PersonalityTrait.builder().trait(i).build())
            .map(i -> i.checkPrimaryTrait(updateRequest.primaryTrait()))
            .limit(LIMIT_OF_SIZE_PERSONALITY_TRAIT)
            .toList();

    List<RelationshipPreference> relationshipPreferenceList =
        updateRequest.relationshipPreferenceConstant().stream()
            .map(i -> RelationshipPreference.builder().relationshipPreference(i).build())
            .toList();
    profile.update(
        updateRequest.nickName(),
        updateRequest.introduction(),
        updateRequest.gender(),
        updateRequest.primaryRole(),
        updateRequest.personalityTraitsToRemove(),
        personalityTraitList,
        updateRequest.relationshipPreferenceConstantToRemove(),
        relationshipPreferenceList);

    profileMonitoringServiceV1.saveProfileRecord(
        profile.getIntroduction(),
        profile.getNickName(),
        profile.getId(),
        profile.getProfileStatus(),
        profile.getUser());

    //TODO 프로필 업데이트 모니터링 테스트
  }

  @Transactional
  public void setProfilePrivate(String name) {
    User user = userServiceV1.findByLoginId(name);
    Profile profile = user.getProfile();
    profile.setPrivate();
  }

  @Transactional
  public void setProfilePublic(String name) {
    User user = userServiceV1.findByLoginId(name);
    Profile profile = user.getProfile();
    profile.setPublic();
  }

  public void banProfile(Long profileId, String rejectedReason, User user) {
    Profile profile = profileRepository.findById(profileId).
        orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_PROFILE));
    profile.setBaned();
    profile.setProfileStatusRejected();
    profileMonitoringServiceV1.saveRejectedProfile(profile, user, rejectedReason);

    UserViolationStats violationStats = user.getViolationStats();
    violationStats.increaseBannedProfileCount();
  }

  public void hideProfile(String setterLoginId, String targetLoginId, LocalDate now) {
    User setter = userServiceV1.findByLoginId(setterLoginId);
    User targetProfile = userServiceV1.findByLoginId(targetLoginId);
    ProfileHidePreference profileHidePreference = ProfileHidePreference.builder()
        .targetUser(targetProfile)
        .settingUser(setter)
        .createAt(now).build();
    profileHidePreferenceRepository.save(profileHidePreference);
  }

  private Profile makesProfile(ProfileRegistration registration, User user) {
    Profile profile = Profile.builder()
        .isPrivate(false)
        .user(user)
        .nickName(registration.nickName())
        .introduction(registration.introduction())
        .gender(registration.gender())
        .primaryRole(registration.primaryRole())
        .birthDate(registration.birthDate())
        .profileStatus(ProfileStatus.NEW)
        .build();

    List<PersonalityTrait> personalityTraitList =
        registration.personalityTraitConstant().stream()
            .map(i -> PersonalityTrait.builder().trait(i).build())
            .map(i -> i.checkPrimaryTrait(registration.primaryTrait()))
            .limit(LIMIT_OF_SIZE_PERSONALITY_TRAIT)
            .toList();

    if (personalityTraitList.stream().noneMatch(PersonalityTrait::isPrimaryTrait)) {
      throw new CustomException(ErrorCode.TRAITS_NOT_INCLUDE_PRIMARY);
    }

    List<RelationshipPreference> relationshipPreferenceList =
        registration.relationshipPreferenceConstant().stream()
            .map(i -> RelationshipPreference.builder().relationshipPreference(i).build())
            .toList();

    profile.addPersonalityTrait(personalityTraitList);
    profile.addRelationshipPreference(relationshipPreferenceList);
    return profile;
  }
}
