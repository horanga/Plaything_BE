package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.user.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class ProfileImageServiceV1 {
    private final ProfileImageRepository profileImageRepository;
    private final ProfileImagesRegistrationServiceV1 profileImagesRegistrationServiceV1;

    @Transactional(rollbackFor = Exception.class)
    public void saveImages(List<SavedImage> images, Profile profile, Long indexOfMainImage) {

        IntStream.range(0, images.size())
                .mapToObj(i -> ProfileImage.builder()
                        .fileName(images.get(i).fileName())
                        .profile(profile)
                        .isMainPhoto(i == indexOfMainImage)
                        .build())
                .forEach(j -> {
                    try {
                        profileImageRepository.save(j);
                        profileImagesRegistrationServiceV1.saveImageRegistration(j);
                    } catch (Exception e) {
                        throw new CustomException(ErrorCode.IMAGE_SAVED_FAILED);
                    }
                });
    }

    public void checkCountOfImages(List<MultipartFile> files, Long profileId) {
        long countOfImages = profileImageRepository.countByProfile_Id(profileId);
        if (countOfImages >= 3 || countOfImages + files.size() > 3) {
            throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
        }
    }


}
