package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.image.service.S3ImagesServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.model.response.MyPageProfile;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileFacadeV1 {

    private final S3ImagesServiceV1 s3ImagesServiceV1;
    private final UserServiceV1 userServiceV1;
    private final ProfileImageServiceV1 profileImageServiceV1;
    private final ProfileQueryServiceV1 profileQueryServiceV1;
    private final ProfileCommandServiceV1 profileCommandServiceV1;

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
    public void uploadImages(List<MultipartFile> files, String name, Long indexOfMainImage) {

        if (files.size() == 0) {
            throw new CustomException(ErrorCode.IMAGE_REQUIRED);
        }

        User user = userServiceV1.findByLoginId(name);
        Profile profile = user.getProfile();

        if (profile == null) {
            throw new CustomException(ErrorCode.NOT_EXIST_PROFILE);
        }
        profileImageServiceV1.checkCountOfImages(files, profile.getId());
        List<SavedImage> savedImages = s3ImagesServiceV1.uploadImages(files);

        try {
            profileImageServiceV1.saveImages(savedImages, profile, indexOfMainImage);
        } catch (Exception e) {
            //롤백 처리
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
