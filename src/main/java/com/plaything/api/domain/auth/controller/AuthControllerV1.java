package com.plaything.api.domain.auth.controller;

import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.model.response.CreateUserResponse;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "V1 Auth API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

    private final AuthService authService;

    @Operation(
            summary = "새로운 유저를 생성합니다.",
            description = "새로운 유저 생성"
    )
    @PostMapping("/create-user")
    public CreateUserResponse creatUser(
            @RequestBody @Valid CreateUserRequest request
            ) {
         return authService.creatUser(request);
    }
    @Operation(
            summary = "로그인 처리",
            description = "로그인을 진행합니다."
    )
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody @Valid LoginRequest loginRequest
            ){
        return authService.login(loginRequest);
    }

    @Operation(
            summary = "get user name",
            description = "token을 기반으로 user를 가져옵니다."
    )
    @GetMapping("/verify-token/{token}")
    public String getUserFromToken(
            @PathVariable("token") String token){
        return authService.getUserFromToken(token);
    }
}

