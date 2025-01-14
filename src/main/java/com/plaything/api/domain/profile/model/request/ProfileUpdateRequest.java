package com.plaything.api.domain.profile.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "유저 사진 변경")
public record ProfileUpdateRequest(

        @Schema(description = "메인 사진 여부")
        List<Boolean> isMainPhoto,
        @Schema(description = "메인 사진으로 변경할 기존 사진의 List 인덱스")
        Integer indexOfMainImage,
        @Schema(description = "기존 사진에서 지울 사진의 List 인덱스")
        List<String> imagesToRemove,
        @Schema(description = "메인 사진을 '메인 사진 자격'을 취소할 것인지 여부")
        boolean shouldCancelMainPhoto) {

}
