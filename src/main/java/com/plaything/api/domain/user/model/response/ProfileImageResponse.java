package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileImageResponse {

    private final String url;
    private final boolean isMainPhoto;

    public static ProfileImageResponse from(ProfileImage image, String url) {
        return new ProfileImageResponse(
                url,
                image.isMainPhoto()
        );
    }

}