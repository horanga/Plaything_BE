package com.plaything.api.domain.matching.service;

import static com.plaything.api.domain.key.constant.RewardConstant.REQUIRED_POINT_KEY;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.common.validator.DuplicateRequestChecker;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.service.PointKeyServiceV1;
import com.plaything.api.domain.matching.model.response.MatchingData;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.profile.service.UserServiceV1;
import com.plaything.api.domain.repository.entity.user.User;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MatchingFacadeV1 {

  private final PointKeyServiceV1 pointKeyServiceV1;
  private final DuplicateRequestChecker requestValidator;
  private final UserServiceV1 userServiceV1;
  private final MatchingServiceV1 matchingServiceV1;
  private final RedisService redisService;

  public List<UserMatching> findMatchingCandidates(String loginId, int duration,
      TimeUnit timeUnit) {
    MatchingData redisCache = redisService.getRedisCache(loginId);
    return redisService.getUserMatchingInfo(redisCache, loginId, duration, timeUnit);
  }

  public void updateLastViewedProfile(String loginId, Long profileId, int countDuration,
      int profileDuration, TimeUnit timeUnit) {
    redisService.updateViewCount(
        loginId,
        profileId,
        countDuration,
        profileDuration,
        timeUnit);
  }

  public void sendMatchingRequest(String requesterLoginId, MatchingRequest matchingRequest,
      String transactionId) {
//        validateMatchingRequest(requesterLoginId, transactionId);
    User requester = userServiceV1.findByLoginId(requesterLoginId);
    User partner = userServiceV1.findByLoginId(matchingRequest.partnerLoginId());
    //TODO fcm 토큰 검증
    matchingServiceV1.creatMatching(requester, partner, transactionId);
  }

  public void acceptMatchingRequest(String requesterLoginId, MatchingRequest matchingRequest,
      String transactionId) {
//        validateMatchingRequest(requesterLoginId, transactionId);
    User matchingReceiver = userServiceV1.findByLoginId(requesterLoginId);
    User matchingSender = userServiceV1.findByLoginId(matchingRequest.partnerLoginId());
    //TODO fcm 토큰 검증
    matchingServiceV1.acceptMatching(matchingReceiver, matchingSender, transactionId);
  }

  private void validateMatchingRequest(String requesterLoginId, String transactionId) {
    if (!requestValidator.checkDuplicateRequest(requesterLoginId, transactionId)) {
      throw new CustomException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
    }
    Long availablePointKey = pointKeyServiceV1.getAvailablePointKey(requesterLoginId);
    if (availablePointKey == null || availablePointKey < REQUIRED_POINT_KEY) {
      throw new CustomException(ErrorCode.NOT_EXIST_AVAILABLE_POINT_KEY);
    }
  }

}
