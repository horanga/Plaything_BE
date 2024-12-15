package com.plaything.api.domain.key.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.key.constant.KeySource;
import com.plaything.api.domain.key.constant.PointStatus;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.repository.entity.pay.PointKey;
import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.pay.UserRewardActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.plaything.api.domain.key.constant.KeySource.ADVERTISEMENT_REWARD;
import static com.plaything.api.domain.key.constant.KeySource.LOGIN_REWARD;
import static com.plaything.api.domain.key.constant.PointStatus.USED;

@RequiredArgsConstructor
@Service
public class PointKeyServiceV1 {

    private final UserRewardActivityRepository userRewardActivityRepository;
    private final PointKeyRepository pointKeyRepository;
    private final AdLogServiceV1 adLogServiceV1;
    private final PointKeyLogServiceV1 pointKeyLogServiceV1;


    //TODO 외부 PG사와 연동하면 복구 작업 필요함
//    try {
//        // 1. 외부 PG사 결제 처리
//        payment = paymentProcessor.processAtPG(request);
//
//        // 2. 내부 DB에 결제 기록
//        paymentRepository.save(payment);
//
//        try {
//            // 3. 포인트 적립
//            pointService.addPoints(payment);

    @Transactional(rollbackFor = Exception.class)
    public void createPointKeyForAd(
            User user,
            int time,
            AdRewardRequest request,
            LocalDateTime now,
            String transactionId) {
        UserRewardActivity userRewardActivity = validateForAd(user, now);
        userRewardActivity.updateLastAdviewTime();
        createKey(user, time, ADVERTISEMENT_REWARD, transactionId);

        try {
            adLogServiceV1.createAdViewLog(user, request);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.LOG_SAVED_FAILED);
        }
    }

    public boolean createPointKeyForLogin(User user, int times, LocalDate now, String transactionId) {
        boolean canReceiveDailyReward = canGetDailyReward(user, now);

        if (canReceiveDailyReward) {
            createKey(user, times, LOGIN_REWARD, transactionId);
        }
        return canReceiveDailyReward;
    }

    private boolean canGetDailyReward(User user, LocalDate now) {
        Optional<UserRewardActivity> adActivity = userRewardActivityRepository.findByUser_loginId(user.getLoginId());

        if (adActivity.isEmpty()) {
            UserRewardActivity userRewardActivity = UserRewardActivity.builder()
                    .user(user)
                    .lastLoginTime(now)
                    .build();
            userRewardActivityRepository.save(userRewardActivity);
            return true;
        }
        UserRewardActivity userRewardActivity = adActivity.get();
        if (userRewardActivity.canReceiveDailyReward(now)) {
            userRewardActivity.updateLastLoginTime(now);
            return true;
        }
        return false;
    }

    public Long getAvailablePointKey(String loginId) {
        return pointKeyRepository.countAvailablePointKey(loginId);
    }

    public void usePointKey(User requester, User partner, String transactionId) {
        PointKey usedKey = PointKey.builder()
                .user(requester)
                .transactionId(transactionId)
                .isValidKey(true)
                .status(USED)
                .build();
        pointKeyRepository.save(usedKey);
        pointKeyLogServiceV1.createKeyUsageLog(requester, partner, usedKey);
    }

    private UserRewardActivity validateForAd(User user, LocalDateTime now) {
        Optional<UserRewardActivity> adActivity = userRewardActivityRepository.findByUser_loginId(user.getLoginId());
        if (adActivity.isEmpty()) {
            UserRewardActivity userRewardActivity = UserRewardActivity.builder()
                    .user(user)
                    .build();
            return userRewardActivityRepository.save(userRewardActivity);
        }

        UserRewardActivity userRewardActivity = adActivity.get();
        if (!userRewardActivity.isMoreThan4HoursPassed(now)) {
            throw new CustomException(ErrorCode.AD_VIEW_TIME_NOT_EXPIRED);
        }
        return userRewardActivity;
    }

    private void createKey(User user, int times, KeySource keySource, String transactionId) {
        LocalDateTime expirationDate = LocalDateTime.now().plusMonths(6);
        for (int i = 0; i < times; i++) {

            PointKey pointKey;

            if (i == 0) {
                pointKey = PointKey.builder()
                        .isValidKey(true)
                        .status(PointStatus.EARN)
                        .expirationDate(expirationDate)
                        .user(user)
                        .transactionId(transactionId) //트랜잭션 id하나로 여러개의 키를 생성해서 중복문제가 발생
                        .build();
                pointKeyRepository.save(pointKey);

            } else {

                pointKey = PointKey.builder()
                        .isValidKey(true)
                        .status(PointStatus.EARN)
                        .expirationDate(expirationDate)
                        .user(user)
                        .transactionId(transactionId + ":" + i) //트랜잭션 id하나로 여러개의 키를 생성해서 중복문제가 발생
                        .build();
                pointKeyRepository.save(pointKey);
            }

            try {
                pointKeyLogServiceV1.createLog(user, expirationDate, pointKey, keySource);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.LOG_SAVED_FAILED);
            }
        }
    }
}
