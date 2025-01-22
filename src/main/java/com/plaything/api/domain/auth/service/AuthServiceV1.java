package com.plaything.api.domain.auth.service;

import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.model.response.LoginResult;
import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserCredentials;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.plaything.api.domain.profile.constants.Role.ROLE_USER;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceV1 {

    private String googleAuthUrl = "https://oauth2.googleapis.com/token";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTProvider jwtProvider;
    private final LoginSuccessHandler loginSuccessHandler;

    @Transactional
    public LoginResult login(String provider, String providerId, String transactionId, String fcmToken) {
        String loginId = provider + providerId;
        User user = userRepository.findByLoginId(loginId).orElse(null);
        if (user == null) {
            user = creatUser(new CreateUserRequest(loginId, provider, fcmToken));
        } else {
            boolean isFckTokenSame = user.getFcmToken().equals(fcmToken);
            if (!isFckTokenSame) {
                user.updateFcmToken(fcmToken);
            }
        }
        String token = jwtProvider.createToken(user.getLoginId(), user.getRole().toString(), 60 * 60 * 1000L);
        LoginResponse loginResponse = loginSuccessHandler.handleSuccessFulLogin(user.getLoginId(), transactionId, LocalDate.now());
        return new LoginResult(token, loginResponse);
    }

    @Transactional
    public User creatUser(CreateUserRequest request) {

//        Optional<User> user = userRepository.findByLoginId(request.loginId());
//
//        if (user.isPresent()) {
//            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
//        }
        User newUser = this.newUser(request.loginId(), request.fcmToken());
        UserCredentials credentials = this.newUserCredentials(request.password(), newUser);
        UserViolationStats userViolation = this.newViolations();

        UserRewardActivity userReward = this.newUserRewardActivity();
        newUser.setCredentials(credentials);
        newUser.setViolationStats(userViolation);
        newUser.setUserRewardActivity(userReward);
        userRepository.save(newUser);

        return newUser;
    }

    private UserRewardActivity newUserRewardActivity() {

        return UserRewardActivity.builder()
                .lastAdViewTime(LocalDateTime.now().minusHours(5)).
                lastLoginTime(LocalDate.now().minusDays(1)).build();
    }

    private UserViolationStats newViolations() {

        return UserViolationStats.builder()
                .bannedImageCount(0)
                .bannedProfileCount(0)
                .reportViolationCount(0).build();
    }

    private User newUser(String name, String fcmToken) {
        return User.builder()
                .loginId(name)
                .role(ROLE_USER)
                .fcmToken(fcmToken)
                .build();
    }

    private UserCredentials newUserCredentials(String password, User user) {
        String hashingValue = bCryptPasswordEncoder.encode(password);

        return UserCredentials.builder()
                .hashedPassword(hashingValue)
                .build();
    }

    public String getAccessToken(String authorizationCode) throws UnsupportedEncodingException {

        String decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8.name());
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", decodedCode);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(googleAuthUrl, request, String.class);

        return response.getBody();
    }
}

