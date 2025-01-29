package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ChatRateLimiter {

  private final Map<String, Integer> counts = new ConcurrentHashMap<>();
  private final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();


  public void checkRate(String nickName) {
    long now = System.currentTimeMillis();

    long lastReset = lastResetTime.computeIfAbsent(nickName, k -> now);

    // 1초가 지났으면 카운트 리셋
    if (now - lastReset >= 1000) {
      counts.put(nickName, 1);
      lastResetTime.put(nickName, now);
      return;
    }

    // 카운트 증가하고 체크
    int newCount = counts.merge(nickName, 1, Integer::sum);
    if (newCount > 3) {
      throw new CustomException(ErrorCode.TOO_MANY_CHAT_RATE);
    }
  }

  public void cleanupOldData() {
    lastResetTime.clear();
    counts.clear();
  }

  public boolean isEmpty() {
    boolean empty = this.lastResetTime.isEmpty();
    boolean empty1 = this.counts.isEmpty();

    return empty && empty1;
  }
}
