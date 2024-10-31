package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User의 사진")
public record ProfileImageResponse(
        @Schema(description = "사진 주소")
        String url
) {
    public static ProfileImageResponse toResponse(ProfileImage profileImage){
        return new ProfileImageResponse(profileImage.getUrl());
    }
}