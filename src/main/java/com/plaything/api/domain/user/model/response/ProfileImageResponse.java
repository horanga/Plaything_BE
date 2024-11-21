package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User의 사진")
public record ProfileImageResponse(
        @Schema(description = "사진 주소")
        String url,

        @Schema(description = "대표사진 여부")
        boolean isMainPhoto
) {
    public static ProfileImageResponse toResponse(ProfileImage profileImage) {
        return new ProfileImageResponse(profileImage.getUrl(), profileImage.isMainPhoto());
    }
}