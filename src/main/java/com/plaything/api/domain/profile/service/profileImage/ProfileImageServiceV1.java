package com.plaything.api.domain.profile.service.profileImage;

import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.repository.entity.monitor.ProfileImageRegistration;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProfileImageServiceV1 {

  private final ProfileRepository profileRepository;

  @Transactional(rollbackFor = Exception.class)
  public void saveImages(List<SavedImage> images, String loginId) {
    Profile profile = profileRepository.findByUser_LoginId(loginId);

    List<ProfileImage> imagesList = images.stream().map(i ->
            ProfileImage.builder().fileName(i.fileName()).isMainPhoto(i.isMainPhoto()).build())
        .toList();
    List<ProfileImage> profileImages = profile.addProfileImages(imagesList);
    profileRepository.flush();

    profileImages.forEach(i -> {
      ProfileImageRegistration registration = ProfileImageRegistration.builder()
          .profileImageId(i.getId()).fileName(i.getFileName()).build();
      i.setProfileImageRegistration(registration);
    });
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateImages(String loginId, List<String> fileNamesToRemove,
      List<SavedImage> newImages, Integer indexOfMainImage, boolean shouldCancelMainPhoto) {

    Profile profile = profileRepository.findByUser_LoginId(loginId);
    //단순히 Main 사진만 변경할 때
    if ((fileNamesToRemove == null || fileNamesToRemove.isEmpty())
        && (newImages == null || newImages.isEmpty())
        && indexOfMainImage != null) {
      profile.updateMainPhoto(indexOfMainImage);
      return;
    }

    List<ProfileImage> imagesList = newImages.stream()
        .map(i ->
            ProfileImage.builder()
                .fileName(i.fileName())
                .isMainPhoto(i.isMainPhoto()).build())
        .toList();

    List<ProfileImage> profileImages = profile.updateProfilePictures(
        fileNamesToRemove,
        imagesList,
        indexOfMainImage,
        shouldCancelMainPhoto);

    profileRepository.flush();

    profileImages.stream()
        .filter(i -> i.getProfileImageRegistration() == null)
        .forEach(i -> {
          ProfileImageRegistration registration =
              ProfileImageRegistration.builder()
                  .profileImageId(i.getId())
                  .fileName(i.getFileName()).build();
          i.setProfileImageRegistration(registration);
        });
  }

}
