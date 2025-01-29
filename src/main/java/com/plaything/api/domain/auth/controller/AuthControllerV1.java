package com.plaything.api.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.plaything.api.domain.auth.client.AppleApiClient;
import com.plaything.api.domain.auth.client.ApplePublicKeyGenerator;
import com.plaything.api.domain.auth.client.GoogleApiClient;
import com.plaything.api.domain.auth.client.constants.OAuth2Provider;
import com.plaything.api.domain.auth.client.dto.request.AppleLoginRequest;
import com.plaything.api.domain.auth.client.dto.request.GoogleLoginRequest;
import com.plaything.api.domain.auth.client.dto.response.ApplePublicKeyResponse;
import com.plaything.api.domain.auth.client.dto.response.GoogleUserInfo;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.model.response.LoginResult;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.security.JWTProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "V1 Auth API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

  private final AuthServiceV1 authServiceV1;
  private final GoogleApiClient googleApiClient;
  private final AppleApiClient appleApiClient;
  private final JWTProvider jwtProvider;
  private final ApplePublicKeyGenerator applePublicKeyGenerator;

  @Operation(
      summary = "새로운 유저를 생성합니다.",
      description = "새로운 유저 생성"
  )
  @PostMapping("/create-user")
  public void createUser(
      @Valid @RequestBody CreateUserRequest request
  ) {
    authServiceV1.createUser(request);
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
  public ResponseEntity<LoginResponse> googleLogin(
      @RequestHeader("Transaction-id") String transactionId,
      @RequestBody @Valid GoogleLoginRequest request) {
    GoogleUserInfo userInfo = googleApiClient.getUserInfo("Bearer " + request.accessToken());
    LoginResult result = authServiceV1.login(OAuth2Provider.GOOGLE, userInfo.getSub(),
        transactionId, request.fcmToken());

    return ResponseEntity.ok()
        .header("Authorization", "Bearer " + result.token())
        .body(new LoginResponse(List.of(result.login())));
  }

  @Operation(
      summary = "애플 로그인",
      description = """
          애플 OAuth2 로그인 기능입니다.
          
          ### 헤더
          1.Transaction-id : 매일 첫 로그인 시 무료 포인트를 제공하기 때문에 중복 요청을 거르는 난수 식별값
          
          ### 바디
          1.identityToken : 애플 identity 토큰
          2.fcmToken : 로그인 마다 fcm 토큰 변경을 위해서 전달
          """
  )
  @PostMapping("/apple/login")
  public ResponseEntity<LoginResponse> appleLogin(
      @RequestHeader("Transaction-id") String transactionId,
      @Valid @RequestBody AppleLoginRequest request)
      throws JsonProcessingException, BadRequestException, NoSuchAlgorithmException, InvalidKeySpecException {
    ApplePublicKeyResponse publicKeys = appleApiClient.getPublicKeys();
    Map<String, String> headers = jwtProvider.parseHeaders(request.identityToken());
    PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers, publicKeys);
    Claims claim = jwtProvider.getTokenClaims(request.identityToken(), publicKey);
    authServiceV1.validateAppleLogin(claim);

    LoginResult result = authServiceV1.login(OAuth2Provider.APPLE, claim.getSubject(),
        transactionId, request.fcmToken());
    return ResponseEntity.ok()
        .header("Authorization", "Bearer " + result.token())
        .body(new LoginResponse(List.of(result.login())));
  }

  @Operation(
      summary = "개발용 로그인 처리",
      description = "로그인을 진행합니다."
  )
  @PostMapping("/login")
  public String login(
      @RequestBody @Valid LoginRequest loginRequest) {
    return authServiceV1.loginForDevelop(loginRequest);
  }

}

