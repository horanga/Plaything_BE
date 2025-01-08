package com.plaything.api.domain.profile.controller;

import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.model.response.ProfileResponse;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "User API", description = "V1 User API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class ProfileControllerV1 {

    private final ProfileFacadeV1 profileFacadeV1;

    @Operation(
            summary = "프로필 등록",
            description = "유저의 신규 프로필을 등록합니다."
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/register-profile")
    public void registerProfile(
            @Valid @RequestBody ProfileRegistration registration,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.registerProfile(registration, user);
    }

    @Operation(
            summary = "Update profile",
            description = "User의 프로필을 변경"
    )
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/update-profile")
    public void updateProfile(
            @Valid @RequestBody ProfileUpdate update,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.updateProfile(update, user);
    }

    @Operation(
            summary = "get profile",
            description = "User의 프로필을 확인"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-profile")
    public ProfileResponse getProfile(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return profileFacadeV1.getProfileByLoginId(user);
    }

    @Operation(
            summary = "특정 이용자의 프로필이 매칭에 포함되지 않도록 합니다",
            description = "매칭 조회 시 특정 이용자의 프로필이 일주일간 포함되지 않도록 설정합니다"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping(value = "/hide-profile/{loginId}")
    public void hideProfile(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString,
            @RequestParam(value = "loginId") String loginId) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.hideProfile(user, loginId, LocalDate.now());
    }


    @Operation(
            summary = "Set profile private",
            description = "User의 프로필 비공개"
    )
    @SecurityRequirement(name = "Authorization")
    @PatchMapping("/set-private")
    public void setProfilePrivate(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.setProfilePrivate(user);
    }


    @Operation(
            summary = "Set profile public",
            description = "User의 프로필 공개"
    )
    @SecurityRequirement(name = "Authorization")
    @PatchMapping("/set-public")
    public void setProfilePublic(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.setProfilePublic(user);
    }

    @Operation(
            summary = "Upload images",
            description = "User의 프로필 사진들을 업로드"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadImage(
            @RequestPart(value = "indexOfMainImage") Long indexOfMainImage,
            @RequestPart(value = "images") List<MultipartFile> images,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.uploadImages(images, user, indexOfMainImage);
    }

    @DeleteMapping("{id}")
    public void deleteUser(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.delete(user);
    }





//
//    @DeleteMapping("/{id}/image")
//    public ResponseEntity<Object> deleteUserImage(@PathVariable Long id,
//                                                  @RequestBody DeleteUserImageRequest request) {
//        userCommandService.deleteUserImage(id, request.getDeleteImageFilename());
//        return ResponseEntity.accepted().build();
//    }
}
