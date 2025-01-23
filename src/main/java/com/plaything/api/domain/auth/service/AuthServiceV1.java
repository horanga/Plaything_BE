package com.plaything.api.domain.auth.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.auth.client.constants.OAuth2Provider;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.model.response.LoginResult;
import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserCredentials;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.security.JWTProvider;
import io.jsonwebtoken.Claims;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static com.plaything.api.domain.auth.client.constants.OAuth2Constants.APPLE_ISSUER;
import static com.plaything.api.domain.profile.constants.Role.ROLE_USER;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceV1 {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTProvider jwtProvider;
    private final LoginSuccessHandler loginSuccessHandler;

    @Transactional
    public LoginResult login(OAuth2Provider provider, String providerId, String transactionId, String fcmToken) {
        if (providerId == null || providerId.isBlank()) {
            throw new CustomException(ErrorCode.AUTHORIZATION_FAIL);
        }

        String loginId = provider.name() + providerId;
        User user = userRepository.findByLoginId(loginId).orElse(null);
        if (user == null) {
            user = creatUser(new CreateUserRequest(loginId, provider.name(), fcmToken));
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

    public void validateAppleLogin(Claims claims) {
        // Claims 검증
        String subject = claims.getSubject();
        if (StringUtils.isBlank(subject)) {
            throw new CustomException(ErrorCode.AUTHORIZATION_FAIL);
        }

        // 토큰 만료 검증
        if (claims.getExpiration().before(new Date())) {
            throw new CustomException(ErrorCode.AUTHORIZATION_FAIL);
        }

        // 발급자 검증
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new CustomException(ErrorCode.AUTHORIZATION_FAIL);
        }

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
}

