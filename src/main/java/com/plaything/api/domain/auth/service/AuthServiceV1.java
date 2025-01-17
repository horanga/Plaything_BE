package com.plaything.api.domain.auth.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.response.CreateUserResponse;
import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserCredentials;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.plaything.api.domain.profile.constants.Role.ROLE_USER;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceV1 {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public CreateUserResponse creatUser(CreateUserRequest request) {

        Optional<User> user = userRepository.findByLoginId(request.loginId());

        if (user.isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        try {
            User newUser = this.newUser(request.loginId(), request.fcmToken());
            UserCredentials credentials = this.newUserCredentials(request.password(), newUser);
            UserViolationStats userViolation = this.newViolations();

            UserRewardActivity userReward = this.newUserRewardActivity();
            newUser.setCredentials(credentials);
            newUser.setViolationStats(userViolation);
            newUser.setUserRewardActivity(userReward);
            userRepository.save(newUser);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.USER_SAVED_FAILED);
        }

        return new CreateUserResponse(ErrorCode.SUCCESS);
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
