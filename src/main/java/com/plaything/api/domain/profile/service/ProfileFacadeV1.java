package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.common.validator.DuplicateRequestChecker;
import com.plaything.api.domain.image.service.S3ImagesServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.profile.model.request.ProfileImageRequest;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.model.response.MyPageProfile;
import com.plaything.api.domain.profile.service.profile.ProfileCommandServiceV1;
import com.plaything.api.domain.profile.service.profile.ProfileQueryServiceV1;
import com.plaything.api.domain.profile.service.profileImage.ProfileImageQueryServiceV1;
import com.plaything.api.domain.profile.service.profileImage.ProfileImageServiceV1;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileFacadeV1 {

    private final S3ImagesServiceV1 s3ImagesServiceV1;
    private final UserServiceV1 userServiceV1;
    private final ProfileImageServiceV1 profileImageServiceV1;
    private final ProfileQueryServiceV1 profileQueryServiceV1;
    private final ProfileCommandServiceV1 profileCommandServiceV1;
    private final ProfileImageQueryServiceV1 profileImageQueryServiceV1;
    private final DuplicateRequestChecker duplicateRequestChecker;
    private final ProfileRepository profileRepository;

    @Transactional
    public void registerProfile(ProfileRegistration registration, String loginId) {
        //TODO 대표성향을 한개만, 상세 성향은 6개까지만, 선호하는 관계는 모든 선택 가능
        //TODO 상세성향 중복 안되도록 변경

        User user = userServiceV1.findByLoginIdForRegistration(loginId);
        profileQueryServiceV1.validateRegistration(registration, user);
        profileCommandServiceV1.registerProfile(registration, user);
    }

    @Transactional
    public void updateProfile(ProfileUpdate updateRequest, String loginId) {
        Profile profile = getProfileByLoginIdNotDTO(loginId);
        profileQueryServiceV1.validateUpdate(updateRequest, profile);
        profileCommandServiceV1.updateProfile(updateRequest, profile);
    }

    @Transactional(readOnly = true)
    public MyPageProfile getMyPageProfile(String loginId) {
        return profileQueryServiceV1.getProfileForMyPage(loginId);
    }

    public void setProfilePrivate(String name) {
        profileCommandServiceV1.setProfilePrivate(name);
    }

    public void setProfilePublic(String name) {
        profileCommandServiceV1.setProfilePublic(name);
    }

    public void banProfile(Long profileId, String rejectedReason, User user) {
        profileCommandServiceV1.banProfile(profileId, rejectedReason, user);
    }

    //TODO 비동기 처리할지 확인하기
    public void registerImages(List<ProfileImageRequest> images, String transactionId, String loginId) {

        boolean isFirst = duplicateRequestChecker.checkImageDuplicateRequest(loginId, transactionId);
        if (!isFirst) {
            throw new CustomException(ErrorCode.DUPLICATE_TRANSACTION_REQUEST);
        }
        //사진 없으면 예외
        profileImageQueryServiceV1.validateImages(images, loginId);
        List<SavedImage> savedImages = s3ImagesServiceV1.uploadImages(images);
        try {
            profileImageServiceV1.saveImages(savedImages, loginId);
        } catch (Exception e) {
            //롤백 처리
            s3ImagesServiceV1.rollbackS3Images(savedImages);
        }
    }

    public void updateImages(String loginId, List<ProfileImageRequest> newImages, String transactionId, List<String> imagesToRemove, Integer indexOfMainImage, boolean shouldCancelMainPhoto) {
        boolean isFirst = duplicateRequestChecker.checkImageDuplicateRequest(loginId, transactionId);

        if (!isFirst) {
            throw new CustomException(ErrorCode.DUPLICATE_TRANSACTION_REQUEST);
        }

        //새로운 사진을 추가하거나, 기존 사진에서 메인 사진을 변경하는 것같은 어떠한 변경 작업도 없는 경우
        if ((newImages == null || newImages.isEmpty())  //새로운 사진 추가 x
                && (imagesToRemove == null || imagesToRemove.isEmpty()) //기존 사진 제거 x
                && (indexOfMainImage == null || !shouldCancelMainPhoto)) //메인 사진 변경 x
        {
            throw new CustomException(ErrorCode.INVALID_IMAGE_UPDATE_REQUEST);
        }

        Profile profile = profileRepository.findByUser_LoginId(loginId);

        profile.validateUpdateRequest(
                imagesToRemove,
                newImages,
                shouldCancelMainPhoto);

        List<SavedImage> savedImages = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            savedImages.addAll(s3ImagesServiceV1.uploadImages(newImages));
        }

        try {
            profileImageServiceV1.updateImages(loginId, imagesToRemove, savedImages, indexOfMainImage, shouldCancelMainPhoto);
            s3ImagesServiceV1.deleteImage(imagesToRemove);

        } catch (Exception e) {
            s3ImagesServiceV1.rollbackS3Images(savedImages);
        }
    }

    public void hideProfile(String setterLoginId, String targetLoginId, LocalDate now) {
        profileCommandServiceV1.hideProfile(setterLoginId, targetLoginId, now);
    }

    public List<String> getHideList(String loginId) {
        return profileQueryServiceV1.getHideList(loginId);
    }

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
