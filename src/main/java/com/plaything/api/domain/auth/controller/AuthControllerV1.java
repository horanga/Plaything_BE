package com.plaything.api.domain.auth.controller;

import com.plaything.api.domain.auth.client.AppleApiClient;
import com.plaything.api.domain.auth.client.GoogleApiClient;
import com.plaything.api.domain.auth.client.dto.request.LoginRequest;
import com.plaything.api.domain.auth.client.dto.response.ApplePublicKeyResponse;
import com.plaything.api.domain.auth.client.dto.response.GoogleUserInfo;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.model.response.LoginResult;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auths", description = "V1 Auth API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {
    private final AuthServiceV1 authServiceV1;
    private final GoogleApiClient googleApiClient;
    private final AppleApiClient appleApiClient;

    @Operation(
            summary = "새로운 유저를 생성합니다.",
            description = "새로운 유저 생성"
    )
    @PostMapping("/create-user")
    public void creatUser(
            @RequestBody @Valid CreateUserRequest request
    ) {
        authServiceV1.creatUser(request);
    }

    @Operation(
            summary = "구글 로그인",
            description = """
                    구글 OAuth2 로그인 기능입니다.
                    
                    ### 헤더
                    1.Transaction-id : 매일 첫 로그인 시 무료 포인트를 제공하기 때문에 중복 요청을 거르는 난수 식별값
                    
                    ### 바디
                    1.accessToken : 정보 요청을 위한 토큰
                    2.fcmToken : 로그인 마다 fcm 토큰 변경을 위해서 전달
                    """
    )
    @PostMapping("/google/login")
    public ResponseEntity<LoginResponse> googleLogin(@RequestHeader("Transaction-id") String transactionId,
                                                     @RequestBody LoginRequest request) {
        GoogleUserInfo userInfo = googleApiClient.getUserInfo("Bearer " + request.accessToken());
        userInfo.setProvider();
        LoginResult result = authServiceV1.login(userInfo.getProvider(), userInfo.getSub(), transactionId, request.fcmToken());

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + result.token())
                .body(result.loginResponse());
    }

    @PostMapping("/apple/login")
    public void appleLogin() {
        ApplePublicKeyResponse publicKeys = appleApiClient.getPublicKeys();
    }
//
//    @GetMapping("/google")
//    public ResponseEntity<String> googleCallback(@RequestParam String authorizationToken) throws UnsupportedEncodingException {
//        String accessToken = authServiceV1.getAccessToken(authorizationToken);
//        return ResponseEntity.ok(accessToken);
//    }
}

