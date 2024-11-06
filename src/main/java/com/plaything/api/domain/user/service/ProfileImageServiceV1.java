package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.user.ProfileImageRepository;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileImageServiceV1 {
    private final ProfileImageRepository profileImageRepository;
    private final ProfileImagesRegistrationServiceV1 profileImagesRegistrationServiceV1;

    @Transactional
    public ProfileImage saveImage(SavedImage image, Profile profile) {

        ProfileImage imageToSave = ProfileImage.builder()
                .url(image.url())
                .fileName(image.fileName())
                .profile(profile)
                .build();
        try {
            ProfileImage profileImage = profileImageRepository.save(imageToSave);
            profileImagesRegistrationServiceV1.saveImageRegistration(imageToSave);
            return profileImage;

        } catch (Exception e) {
            throw new CustomException(ErrorCode.IMAGE_SAVED_FAILED);
        }
    }

    public long checkCountOfImages(Long profileId){
       return profileImageRepository.countByProfile_Id(profileId);
    }
}
