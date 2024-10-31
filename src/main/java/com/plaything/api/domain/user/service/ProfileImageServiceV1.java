package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.user.ProfileImageRepository;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileImageServiceV1 {
    private final ProfileImageRepository profileImageRepository;
    private final ProfileImagesRegistrationServiceV1 profileImagesRegistrationServiceV1;

    @Transactional
    public void saveImage(SavedImage image, User user) {

        ProfileImage imageToSave = ProfileImage.builder()
                .url(image.url())
                .fileName(image.fileName())
                .user(user)
                .build();
        try {
            profileImageRepository.save(imageToSave);
            profileImagesRegistrationServiceV1.saveImageRegistration(imageToSave);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.IMAGE_SAVED_FAILED);
        }
    }

    public List<ProfileImageResponse> getProfileImages(User user){
        return profileImageRepository.findByUser(user)
                .stream().map(ProfileImageResponse::toResponse).toList();
    }

    public long checkCountOfImages(String user){
       return profileImageRepository.countByUser_name(user);
    }
}
