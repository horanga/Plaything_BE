package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringServiceV1;
import com.plaything.api.domain.image.service.S3ImagesServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.RelationshipPreference;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.ProfileStatus;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.request.ProfileUpdate;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import com.plaything.api.domain.user.model.response.ProfileResponse;
import com.plaything.api.domain.user.util.ImageUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileFacadeV1 {

    private final ProfileRepository profileRepository;
    private final S3ImagesServiceV1 s3ImagesServiceV1;
    private final UserServiceV1 userServiceV1;
    private final ProfileImageServiceV1 profileImageServiceV1;
    private final ProfileMonitoringServiceV1 profileMonitoringServiceV1;
    private final ImageUrlGenerator imageUrlGenerator;

    @Transactional
    public void registerProfile(ProfileRegistration registration, String name) {
        User user = userServiceV1.findByLoginIdForRegistration(name);

        //TODO 대표성향을 한개만, 상세 성향은 6개까지만, 선호하는 관계는 모든 선택 가능
        //TODO 중복 안되도록 변경

        if (user.getProfile() != null) {
            throw new CustomException(ErrorCode.PROFILE_ALREADY_EXIST);
        }
        if (registration.primaryRole().equals(PrimaryRole.TOP) || registration.primaryRole().equals(PrimaryRole.BOTTOM)) {
            validateTraits(registration);
        }
        try {
            Profile profile = makesProfile(registration, user);
            user.setProfile(profile);
            profileRepository.save(profile);
            profileMonitoringServiceV1.saveProfileRecord(profile, user);
        } catch (CustomException e) {
            throw new CustomException(ErrorCode.TRAITS_NOT_INCLUDE_PRIMARY);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PROFILE_REGISTER_FAILED);
        }
    }

    @Transactional
    public void updateProfile(ProfileUpdate update, String user) {
        Profile profile = getProfileByLoginIdNotDTO(user);

    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByLoginId(String loginId) {
        User user = userServiceV1.findByLoginId(loginId);
        Profile profile = user.getProfile();

        if (profile == null) {
            throw new CustomException(ErrorCode.NOT_EXIST_PROFILE);
        }

        if (profile.isBaned()) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_PROFILE);
        }

        List<ProfileImageResponse> profileImageResponseList = profile.getProfileImages().stream().map(i -> new ProfileImageResponse(imageUrlGenerator.getImageUrl(i.getFileName()),
                i.isMainPhoto())).toList();

        return ProfileResponse.toResponse(profile, profileImageResponseList);
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

    @Transactional
    public void banProfile(Long profileId, String rejectedReason, User user) {
        Profile profile = profileRepository.findById(profileId).
                orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_PROFILE));
        profile.setBaned();
        profile.setProfileStatusRejected();

        profileMonitoringServiceV1.saveRejectedProfile(profile, user, rejectedReason);
        userServiceV1.increaseBannedProfileCount(user);
    }

    //TODO 비동기 처리할지 확인하기
    public void uploadImages(List<MultipartFile> files, String name, Long indexOfMainImage) {

        if (files.size() == 0) {

            throw new CustomException(ErrorCode.IMAGE_REQUIRED);
        }

        User user = userServiceV1.findByLoginId(name);
        Profile profile = user.getProfile();
        profileImageServiceV1.checkCountOfImages(files, profile.getId());
        List<SavedImage> savedImages = s3ImagesServiceV1.uploadImages(files);

        try {
            profileImageServiceV1.saveImages(savedImages, profile, indexOfMainImage);
        } catch (Exception e) {
            //롤백 처리
            s3ImagesServiceV1.rollbackS3Images(savedImages);
        }
    }

    private void validateTraits(ProfileRegistration registration) {
        registration.personalityTraitConstant().forEach(i -> i.validateRoleCompatibility(registration.primaryRole()));
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
                        .map(i -> PersonalityTrait.builder().trait(i).profile(profile).build())
                        .map(i -> i.checkPrimaryTrait(registration.primaryTrait()))
                        .limit(6)
                        .toList();

        if (personalityTraitList.stream().noneMatch(PersonalityTrait::isPrimaryTrait)) {
            throw new CustomException(ErrorCode.TRAITS_NOT_INCLUDE_PRIMARY);
        }

        List<RelationshipPreference> relationshipPreferenceList =
                registration.relationshipPreferenceConstant().stream()
                        .map(i -> RelationshipPreference.builder().relationshipPreference(i).profile(profile).build())
                        .toList();

        profile.addPersonalityTrait(personalityTraitList);
        profile.addRelationshipPreference(relationshipPreferenceList);
        return profile;
    }

    //프로필 조회를 위한 api가 아닌 단순 로직에 필요한 메서드
    public Profile getProfileByLoginIdNotDTO(String loginId) {

        User user = userServiceV1.findByLoginId(loginId);
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new CustomException(ErrorCode.NOT_EXIST_USER);
        }

        if (profile.isBaned()) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_PROFILE);
        }
        return profile;
    }

    public void delete(String auth) {
        userServiceV1.delete(auth);

    }
}
