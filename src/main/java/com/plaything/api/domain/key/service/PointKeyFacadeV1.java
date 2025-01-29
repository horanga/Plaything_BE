package com.plaything.api.domain.key.service;

import static com.plaything.api.domain.key.constant.RewardConstant.REWARD_COUNT_FOR_AD;
import static com.plaything.api.domain.key.constant.RewardConstant.REWARD_COUNT_FOR_FIRST_LOGIN;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.common.validator.DuplicateRequestChecker;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.key.model.response.PointKeyLog;
import com.plaything.api.domain.profile.service.UserServiceV1;
import com.plaything.api.domain.repository.entity.user.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointKeyFacadeV1 {

  private final UserServiceV1 userServiceV1;
  private final PointKeyLogServiceV1 pointKeyLogServiceV1;
  private final PointKeyServiceV1 pointKeyServiceV1;
  private final DuplicateRequestChecker duplicateRequestChecker;

  public void createPointKeyForAd(String loginId, AdRewardRequest request, LocalDateTime now,
      String transactionId) {
    if (!duplicateRequestChecker.checkDuplicateRequest(loginId, transactionId)) {
      throw new CustomException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
    }

    User user = userServiceV1.findByLoginId(loginId);
    pointKeyServiceV1.createPointKeyForAd(user, REWARD_COUNT_FOR_AD, request, now, transactionId);
  }

  public boolean createPointKeyForLogin(User user, String transactionId, LocalDate now) {
    if (!duplicateRequestChecker.checkDuplicateRequest(user.getLoginId(), transactionId)) {
      throw new CustomException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
    }
    return pointKeyServiceV1.createPointKeyForLogin(user, REWARD_COUNT_FOR_FIRST_LOGIN, now,
        transactionId);
  }

  public List<PointKeyLog> getPointKeyLog(String name) {
    return pointKeyLogServiceV1.getPointKeyLog(name);
  }

  public AvailablePointKey getAvailablePointKey(String name) {
    Long availablePointKey = pointKeyServiceV1.getAvailablePointKey(name);

    if (availablePointKey == null) {
      return new AvailablePointKey(0L);
    }
    return new AvailablePointKey(availablePointKey);
  }
}
