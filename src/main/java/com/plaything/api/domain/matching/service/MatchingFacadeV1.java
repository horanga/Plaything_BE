package com.plaything.api.domain.matching.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.common.validator.DuplicateRequestChecker;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.service.PointKeyServiceV1;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.user.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.plaything.api.domain.key.constant.RewardConstant.REQUIRED_POINT_KEY;

@RequiredArgsConstructor
@Service
public class MatchingFacadeV1 {

    private final PointKeyServiceV1 pointKeyServiceV1;
    private final DuplicateRequestChecker duplicateRequestChecker;
    private final UserServiceV1 userServiceV1;
    private final MatchingServiceV1 matchingServiceV1;

    public void createMatching(String requesterLoginId, MatchingRequest matchingRequest, String transactionId) {
        if (!duplicateRequestChecker.checkDuplicateRequest(requesterLoginId, transactionId)) {
            throw new CustomException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }

        Long availablePointKey = pointKeyServiceV1.getAvailablePointKey(requesterLoginId);
        if (availablePointKey == null || availablePointKey < REQUIRED_POINT_KEY) {
            throw new CustomException(ErrorCode.NOT_EXIST_AVAILABLE_POINT_KEY);
        }
        User requester = userServiceV1.findByLoginId(requesterLoginId);
        Profile requesterProfile = requester.getProfile();
        User partner = userServiceV1.findByProfileNickname(matchingRequest.partnerNickname());
        //fcm 토큰 검증
        matchingServiceV1.creatMatching(requester, requesterProfile, partner, transactionId);
    }


    public void acceptMatching(String requesterLoginId, MatchingRequest matchingRequest, String transactionId) {
        if (!duplicateRequestChecker.checkDuplicateRequest(requesterLoginId, transactionId)) {
            throw new CustomException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }

        Long availablePointKey = pointKeyServiceV1.getAvailablePointKey(requesterLoginId);
        if (availablePointKey == null || availablePointKey < REQUIRED_POINT_KEY) {
            throw new CustomException(ErrorCode.NOT_EXIST_AVAILABLE_POINT_KEY);
        }
        User matchingReceiver = userServiceV1.findByLoginId(requesterLoginId);
        User matchingSender = userServiceV1.findByProfileNickname(matchingRequest.partnerNickname());
        //fcm 토큰 검증
        matchingServiceV1.acceptMatching(matchingReceiver, matchingSender, transactionId);
    }

}
