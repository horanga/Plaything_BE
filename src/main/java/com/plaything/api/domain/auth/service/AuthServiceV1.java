package com.plaything.api.domain.auth.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.model.response.CreateUserResponse;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserCredentials;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.security.Hasher;
import com.plaything.api.security.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static com.plaything.api.domain.profile.constants.Role.ROLE_USER;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceV1 {

    private final UserRepository userRepository;
    private final Hasher hasher;
    private final PointKeyFacadeV1 pointKeyFacadeV1;

    public String getUserFromToken(String token) {
        return JWTProvider.getUserFromToken(token);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest loginRequest, LocalDate now, String transactionId) {
        Optional<User> user = userRepository.findByLoginId(loginRequest.loginId());

        if (!user.isPresent()) {
            log.error("NOT_EXIST_USER: {}", loginRequest.loginId());
            throw new CustomException(ErrorCode.NOT_EXIST_USER);
        }

        user.map(u -> {
            String hashingValue = hasher.getHashingValue(loginRequest.password());
            if (!u.getCredentials().getHashedPassword().equals(hashingValue)) {
                throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
            }
            return hashingValue;

        }).orElseThrow(() -> {
            throw new CustomException(ErrorCode.MIS_MATCH_PASSWORD);
        });

        //TODO JWT

        String token = JWTProvider.createToken(loginRequest.loginId());

        //이용자가 프로필을 설정했는지 안했는지 확인
        boolean invalidProfile = user.get().isProfileEmpty();

        if (!invalidProfile && user.get().isPreviousProfileRejected()) {
            invalidProfile = true;
        }

        //그날 첫 로그인이면 Point Key를 1개 제공

        boolean canReceiveDailyReward = false;
        if (pointKeyFacadeV1.createPointKeyForLogin(user.get(), transactionId, now)) {
            canReceiveDailyReward = true;
        }
        return new LoginResponse(ErrorCode.SUCCESS, token, invalidProfile, canReceiveDailyReward);
    }

    @Transactional
    public CreateUserResponse creatUser(CreateUserRequest request) {

        Optional<User> user = userRepository.findByLoginId(request.loginId());

        if (user.isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        try {
            User newUser = this.newUser(request.loginId(), request.fcmToken());
            UserCredentials newCredentials = this.newUserCredentials(request.password(), newUser);
            UserViolationStats userViolationStats = UserViolationStats.builder()
                    .bannedImageCount(0)
                    .bannedProfileCount(0)
                    .reportViolationCount(0).build();
            newUser.setCredentials(newCredentials);
            newUser.setViolationStats(userViolationStats);

            User savedUser = userRepository.save(newUser);

            if (savedUser == null) {
                throw new CustomException(ErrorCode.USER_SAVED_FAILED);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.USER_SAVED_FAILED);
        }

        return new CreateUserResponse(ErrorCode.SUCCESS);
    }

    private User newUser(String name, String fcmToken) {
        return User.builder()
                .loginId(name)
                .role(ROLE_USER)
                .fcmToken(fcmToken)
                .build();
    }

    private UserCredentials newUserCredentials(String password, User user) {
        String hashingValue = hasher.getHashingValue(password);

        return UserCredentials.builder()
                .hashedPassword(hashingValue)
                .build();
    }
}
