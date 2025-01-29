package com.plaything.api.domain.key.service;

import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.response.AdViewLogResponse;
import com.plaything.api.domain.repository.entity.log.AdViewLog;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.log.AdViewLogRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdLogServiceV1 {

  private final AdViewLogRepository adViewLogRepository;

  public void createAdViewLog(User user, AdRewardRequest request) throws IOException {
    AdViewLog adViewLog = AdViewLog.builder()
        .viewDuration(request.viewDuration())
        .adType(request.adType())
        .user(user)
        .build();
    adViewLogRepository.save(adViewLog);
  }

  public List<AdViewLogResponse> getAdViewLog(String loginId) {
    return adViewLogRepository.findByUser_LoginId(loginId).stream()
        .map(AdViewLogResponse::toResponse)
        .toList();
  }
}
