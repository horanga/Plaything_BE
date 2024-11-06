package com.plaything.api.domain.user.controller;

import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.request.ProfileUpdate;
import com.plaything.api.domain.user.model.response.ProfileResponse;
import com.plaything.api.domain.user.model.response.UserSearchResponse;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.UserServiceV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "User API", description = "V1 User API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserControllerV1 {

    private final UserServiceV1 userServiceV1;
    private final ProfileFacadeV1 profileFacadeV1;

    @Operation(
            summary = "Register profile",
            description = "User의 프로필을 등록"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/register-profile")
    public void registerProfile(
            @Valid @RequestBody ProfileRegistration registration,
            @RequestHeader(value = "Authorization", required = false) String authString
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
            @RequestHeader(value = "Authorization", required = false) String authString
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
            @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return profileFacadeV1.getProfile(user);
    }

    @Operation(
            summary = "Set profile private",
            description = "User의 프로필 비공개"
    )
    @SecurityRequirement(name = "Authorization")
    @PatchMapping("/set-private")
    public void setProfilePrivate(
            @RequestHeader(value = "Authorization", required = false) String authString
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
            @RequestHeader(value = "Authorization", required = false) String authString
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
            @RequestParam(value = "images") List<MultipartFile> images,
            @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        profileFacadeV1.uploadImages(images, user);
    }
    @Operation(
            summary = "User Name List Search",
            description = "User Name 기반으로 Like 검색 실행"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/search/{name}")
    public UserSearchResponse searchUser(
            @PathVariable("name") String name,
            @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);

        return userServiceV1.searchUser(name, user);
    }

//
//    @DeleteMapping("/{userId}/image")
//    public ResponseEntity<Object> deleteUserImage(@PathVariable Long userId,
//                                                  @RequestBody DeleteUserImageRequest request) {
//        userCommandService.deleteUserImage(userId, request.getDeleteImageFilename());
//        return ResponseEntity.accepted().build();
//    }
}
