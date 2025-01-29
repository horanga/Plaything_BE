package com.plaything.api.domain.profile.model.request;


import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "유저 사진 등록")
public record ProfileImageRequest(

    @Schema(description = "사진 파일")
    MultipartFile file,

    @Schema(description = "메인 프로필 여부")
    boolean isMainPhoto) {

}

