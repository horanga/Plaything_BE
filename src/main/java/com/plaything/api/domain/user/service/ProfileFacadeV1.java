package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.image.service.S3ImagesServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.RelationshipPreference;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.request.ProfileUpdate;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import com.plaything.api.domain.user.model.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileFacadeV1 {

    private final S3ImagesServiceV1 s3ImagesServiceV1;
    private final UserServiceV1 userServiceV1;
    private final ProfileImageServiceV1 profileImageServiceV1;

    @Transactional
    public void registerProfile(ProfileRegistration registration, String name){
        User user = userServiceV1.findByName(name);

        //TODO 대표성향을 한개만, 상세 성향은 6개까지만, 선호하는 관계는 모든 선택 가능
        //TODO 중복 안되도록 변경

        if(user.getProfile()!=null){
            throw new CustomException(ErrorCode.PROFILE_ALREADY_EXIST);
        }

        try {
            Profile profile = getProfileFromDb(registration);
            user.setProfile(profile);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PROFILE_REGISTER_FAILED);
        }
    }

    @Transactional
    public void updateProfile(ProfileUpdate update, String user) {
        Profile profile = getProfileFromDb(user);

    }

    public ProfileResponse getProfile(String name){
        User user = userServiceV1.findByName(name);
        Profile profile = user.getProfile();
        List<ProfileImageResponse> profileImages = profileImageServiceV1.getProfileImages(user);

        return ProfileResponse.toResponse(profile, profileImages);
    }

    public Profile getProfileFromDb(String name){

        User user = userServiceV1.findByName(name);
        Profile profile = user.getProfile();
        if(profile == null){
            throw new CustomException(ErrorCode.NOT_EXIST_USER);
        }
        return profile;
    }

    public void setProfilePrivate(String name) {
        User user = userServiceV1.findByName(name);
        Profile profile = user.getProfile();
        profile.setPrivate();

    }

    public void setProfilePublic(String name) {
        User user = userServiceV1.findByName(name);
        Profile profile = user.getProfile();
        profile.setPrivate();
    }

    //TODO 비동기 처리할지 확인하기
    @Transactional
    public void uploadImages(List<MultipartFile> files, String name){

        if(files.size()==0){

            throw new CustomException(ErrorCode.NO_IMAGE_FAILED);
        }
        long countOfImages = profileImageServiceV1.checkCountOfImages(name);
        if( countOfImages>=6 || countOfImages+files.size()>6 ){
            throw new CustomException(ErrorCode.EXCEED_IMAGE_LIMIT);
        }
        User user = userServiceV1.findByName(name);
       getProfileFromDb(user.getName());
        List<SavedImage> savedImages = s3ImagesServiceV1.uploadFiles(files);
             savedImages.forEach(image ->
                profileImageServiceV1.saveImage(image, user));
    }



    private Profile getProfileFromDb(ProfileRegistration registration) {

        Profile profile = Profile.builder()
                .isPrivate(false)
                .nickName(registration.nickName())
                .introduction(registration.introduction())
                .gender(registration.gender())
                .primaryRole(registration.primaryRole())
                .birthDate(registration.birthDate())
                .build();
        List<PersonalityTrait> personalityTraitList =
                registration.personalityTraitConstant().stream()
                        .map(i -> PersonalityTrait.builder().personalityTrait(i).profile(profile).build())
                        .limit(6)
                        .toList();

        List<RelationshipPreference> relationshipPreferenceList =
                registration.relationshipPreferenceConstant().stream()
                        .map(i -> RelationshipPreference.builder().relationshipPreference(i).profile(profile).build())
                        .toList();

        profile.setPersonalityTrait(personalityTraitList);
        profile.setRelationshipPreference(relationshipPreferenceList);

        return profile;

    }

}
