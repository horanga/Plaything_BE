package com.plaything.api.domain.matching.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.common.validator.DuplicateRequestChecker;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.service.PointKeyServiceV1;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.user.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.plaything.api.domain.key.constant.RewardConstant.REQUIRED_POINT_KEY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.*;

@RequiredArgsConstructor
@Service
public class MatchingFacadeV1 {


    private final PointKeyServiceV1 pointKeyServiceV1;
    private final DuplicateRequestChecker duplicateRequestChecker;
    private final UserServiceV1 userServiceV1;
    private final MatchingServiceV1 matchingServiceV1;
    private final RedisService redisService;

    public List<UserMatching> searchMatchingPartner(String loginId, int duration, TimeUnit timeUnit) {
        return redisService.searchMatchingPartner(loginId, duration, timeUnit);
    }

    public void countLastProfileId(String loginId, Long profileId, LocalDateTime now) {
        redisService.incrementSkipCount(
                loginId,
                profileId,
                EXPIRATION_DATE_SKIP_COUNT,
                EXPIRATION_DATE_PROFILE_ID,
                CACHE_DURATION_UNIT_DAYS);
    }

    public void createMatching(String requesterLoginId, MatchingRequest matchingRequest, String transactionId) {
        validateMatchingRequest(requesterLoginId, transactionId);
        User requester = userServiceV1.findByLoginId(requesterLoginId);
        User partner = userServiceV1.findByLoginId(matchingRequest.partnerLoginId());
        //TODO fcm 토큰 검증
        matchingServiceV1.creatMatching(requester, partner, transactionId);
    }

    public void acceptMatching(String requesterLoginId, MatchingRequest matchingRequest, String transactionId) {
        validateMatchingRequest(requesterLoginId, transactionId);
        User matchingReceiver = userServiceV1.findByLoginId(requesterLoginId);
        User matchingSender = userServiceV1.findByLoginId(matchingRequest.partnerLoginId());
        //TODO fcm 토큰 검증
        matchingServiceV1.acceptMatching(matchingReceiver, matchingSender, transactionId);
    }

    private void validateMatchingRequest(String requesterLoginId, String transactionId) {
        if (!duplicateRequestChecker.checkDuplicateRequest(requesterLoginId, transactionId)) {
            throw new CustomException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }
        Long availablePointKey = pointKeyServiceV1.getAvailablePointKey(requesterLoginId);
        if (availablePointKey == null || availablePointKey < REQUIRED_POINT_KEY) {
            throw new CustomException(ErrorCode.NOT_EXIST_AVAILABLE_POINT_KEY);
        }
    }

}
