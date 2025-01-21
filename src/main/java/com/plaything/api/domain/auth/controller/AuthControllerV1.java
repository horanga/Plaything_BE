package com.plaything.api.domain.auth.controller;

import com.plaything.api.domain.auth.client.google.GoogleApiClient;
import com.plaything.api.domain.auth.client.google.dto.GoogleUserInfo;
import com.plaything.api.domain.auth.client.google.dto.request.LoginRequest;
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

@Tag(name = "Auth API", description = "V1 Auth API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

    private final AuthServiceV1 authServiceV1;
    private final GoogleApiClient googleApiClient;

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

//    @PostMapping("/apple/login")
//    public ResponseEntity<LoginResponse> appleLogin(@RequestHeader("Transaction-id") String transactionId,
//                                                    @RequestBody LoginRequest request) {
//        GoogleUserInfo userInfo = googleApiClient.getUserInfo("Bearer " + request.accessToken());
//        userInfo.setProvider();
//        LoginResult result = authServiceV1.login(userInfo.getProvider(), userInfo.getSub(), transactionId, request.fcmToken());
//
//        return ResponseEntity.ok()
//                .header("Authorization", "Bearer " + result.token())
//                .body(result.loginResponse());
//    }
//
//    @GetMapping("/google")
//    public ResponseEntity<String> googleCallback(@RequestParam String authorizationToken) throws UnsupportedEncodingException {
//        String accessToken = authServiceV1.getAccessToken(authorizationToken);
//        return ResponseEntity.ok(accessToken);
//    }
}

