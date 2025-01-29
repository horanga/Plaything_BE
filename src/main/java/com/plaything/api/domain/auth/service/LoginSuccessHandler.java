package com.plaything.api.domain.auth.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.auth.model.response.Login;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler {

  private final UserRepository userRepository;
  private final PointKeyFacadeV1 pointKeyFacadeV1;

  @Transactional(rollbackFor = Exception.class)
  public Login handleSuccessFulLogin(String userName, String transactionId, LocalDate now) {

    if (transactionId == null) {
      throw new CustomException(ErrorCode.TRANSACTION_ID_REQUIRED);
    }

    User user = userRepository.findByLoginId(userName).get();

    //이용자가 프로필을 설정했는지 안했는지 확인
    boolean invalidProfile = user.isProfileEmpty();

    if (!invalidProfile && user.isPreviousProfileRejected()) {
      invalidProfile = true;
    }

    //그날 첫 로그인이면 Point Key를 1개 제공
    boolean canReceiveDailyReward = pointKeyFacadeV1.createPointKeyForLogin(user, transactionId,
        now);
    return new Login(ErrorCode.SUCCESS, invalidProfile, canReceiveDailyReward);
  }
}
