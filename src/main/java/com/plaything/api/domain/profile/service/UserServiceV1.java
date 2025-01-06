package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.monitor.UserViolationStatsRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceV1 {

    private final UserRepository userRepository;
    private final UserViolationStatsRepository userViolationStatsRepository;

    public User findByLoginIdForRegistration(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));
    }


    @Transactional(readOnly = true)
    public User findByLoginId(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

        if (user.isProfileEmpty()) {
            throw new CustomException(ErrorCode.NOT_EXIST_PROFILE);
        }

        if (user.isPreviousProfileRejected()) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_PROFILE);
        }
        return user;
    }

    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));
    }

    @Transactional
    public void delete(String auth) {
        User user = userRepository.findByLoginId(auth).get();
        user.delete();
    }

    @Transactional
    public void increaseBannedProfileCount(User user) {
        Optional<UserViolationStats> violationStats = userViolationStatsRepository.findByUser(user);
        if (violationStats.isEmpty()) {
            UserViolationStats userViolationStats = UserViolationStats.builder().user(user)
                    .bannedImageCount(0L)
                    .bannedProfileCount(1L)
                    .reportViolationCount(0L)
                    .user(user)
                    .build();
            userViolationStatsRepository.save(userViolationStats);
        } else {
            violationStats.get().increaseBannedProfileCount();
        }
    }
}
