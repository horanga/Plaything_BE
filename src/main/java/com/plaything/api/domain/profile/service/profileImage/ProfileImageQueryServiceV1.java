package com.plaything.api.domain.profile.service.profileImage;

import static com.plaything.api.domain.profile.constants.Constants.LIMIT_OF_COUNT_OF_PROFILE_IMAGES;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.profile.model.request.ProfileImageRequest;
import com.plaything.api.domain.profile.service.UserServiceV1;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileImageQueryServiceV1 {

  private final UserServiceV1 userServiceV1;

  public void validateImages(List<ProfileImageRequest> images, String loginId) {

    if (images == null || images.isEmpty()) {
      throw new CustomException(ErrorCode.IMAGE_REQUIRED);
    }

    boolean hasMainPhoto = images.stream()
        .anyMatch(ProfileImageRequest::isMainPhoto);

    if (!hasMainPhoto) {
      throw new CustomException(ErrorCode.MAIN_IMAGE_REQUIRED);
    }

    long countOfMainPhoto = images.stream()
        .filter(ProfileImageRequest::isMainPhoto).count();

    if (countOfMainPhoto > 1) {
      throw new CustomException(ErrorCode.MAIN_IMAGE_COUNT_EXCEEDED);
    }

    User user = userServiceV1.findByLoginId(loginId);
    Profile profile = user.getProfile();

    if (profile == null) {
      throw new CustomException(ErrorCode.NOT_EXIST_PROFILE);
    }

    if (images.size() > LIMIT_OF_COUNT_OF_PROFILE_IMAGES) {
      throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
    }
  }
}
