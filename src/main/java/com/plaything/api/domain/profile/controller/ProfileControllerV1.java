package com.plaything.api.domain.profile.controller;

import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.model.response.MyPageProfile;
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

import java.util.List;

@Tag(name = "프로필 API", description = "V1 프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class ProfileControllerV1 {

    private final ProfileFacadeV1 profileFacadeV1;

    @Operation(
            summary = "프로필 등록",
            description = """
                    유저의 신규 프로필을 등록합니다. 유저는 프로필을 등록해야 다른 기능들을 사용할 수 있습니다.
                    
                    1) 상세성향 : TOP, BOTTOM에 맞는 상세성향만 설정해야 합니다.
                    상세 성향에서 대표 상세 성향도 골라야 합니다.
                    
                    ## 예외
                    
                    (1)400 Bad Request
                    - Primary Role와 매칭되지 않는 상세 성향을 골랐을 때
                    - 상세 성향 중에서 대표 성향을 선택하지 않았을 때
                    
                    2) 모니터링 : 프로필을 등록할 시 모니터링에 필요한 내용들이 record로 남습니다.
                    """
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
            summary = "프로필 업데이트",
            description = """
                    유저의 프로필을 변경합니다.
                    1) 상세성향 : TOP, BOTTOM에 맞는 상세성향만 설정해야 합니다.
                       상세 성향에서 대표 상세 성향도 골라야 합니다.
                    
                    ## 예외
                    
                    (1)400 Bad Request
                    - Primary Role와 매칭되지 않는 상세 성향을 골랐을 때
                    - 변경 후 상세 성향 중에서 대표 성향이 없을 때
                    
                    (2)409 Confict
                    - 변경하려는 닉네임이 이미 존재할 때
                    - 대표 성향을 2개 이상 골랐을 때
                    """
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
            summary = "프로필 조회 API",
            description = """
                    MyPage에서 자신의 프로필을 조회합니다.
                    보유 열쇠 갯수, 마지막으로 광고를 본 시간을 전달합니다.
                    클라이언트에서 마지막 광고 시청 시간에 맞춰서 몇 분 뒤에 광고를 다시 볼 수 있을지 계산합니다.
                    
                    ## 예외
                    
                    (1) 403 Not Authorized
                    - 프로필이 금지된 상태에서 조회를 누를 때
                    
                    (2) 404 Not Found
                    -프로필이 없는 상태에서 조회를 누를 때
                    
                    """
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-profile")
    public MyPageProfile getProfile(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return profileFacadeV1.getMyPageProfile(user);
    }

    @Operation(
            summary = "프로필 비공개",
            description = """
                    자신의 프로필이 매칭 알고리즘에 포함되지 않도록 비공개합니다.
                    """
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
            summary = "프로필 공개",
            description = """
                    자신의 프로필이 매칭 알고리즘에 포함되도록 공개합니다.
                    """
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
            summary = "사진 업로드",
            description = """
                    유저 프로필 사진을 업로드합니다. 최대 3장까지 업로드할 수 있습니다.
                    
                    ## 예외
                    (1) 400 Bad Request
                    -등록할 사진이 없는데 요청을 보냈을 때
                    -등록할 사진이 3개를 초과했을 때
                    
                    (2) 404 Not Found
                    -프로필을 등록하지 않은 회원일 때
                    """
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
