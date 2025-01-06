package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.monitor.UserViolationStatsRepository;
import com.plaything.api.domain.profile.model.response.UserStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserViolationServiceV1 {

    private final UserViolationStatsRepository userViolationStatsRepository;

    public UserStats getUserStats(User user){
        UserViolationStats userViolationStats = userViolationStatsRepository.findByUser(user).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_EXIST_USER)
        );
      return   UserStats.toResponse(userViolationStats);
    }


}
