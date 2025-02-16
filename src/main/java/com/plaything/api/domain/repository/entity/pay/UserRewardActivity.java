package com.plaything.api.domain.repository.entity.pay;

import static com.plaything.api.domain.key.constant.RewardConstant.AD_VIEW_INTERVAL_HOURS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserRewardActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime lastAdViewTime;

  @Column(columnDefinition = "TIMESTAMP(0)")
  private LocalDate lastLoginTime;


  public boolean isMoreThan4HoursPassed(LocalDateTime localDateTime) {
    if (lastAdViewTime == null) {
      updateLastAdViewTime();
      return true;
    }
    return Duration.between(this.lastAdViewTime, localDateTime).toHours() >= AD_VIEW_INTERVAL_HOURS;
  }

  public void updateLastAdviewTime() {
    this.lastAdViewTime = LocalDateTime.now();
  }

  // 일일 보상 수령 가능 여부 체크
  public boolean canReceiveDailyReward(LocalDate now) {
    return lastLoginTime == null ||
        Period.between(this.lastLoginTime, now).getDays() >= 1;
  }

  // 광고 시청 시간 업데이트
  public void updateLastAdViewTime() {
    this.lastAdViewTime = LocalDateTime.now();
  }

  // 로그인 시간 업데이트
  public void updateLastLoginTime(LocalDate now) {
    this.lastLoginTime = now;
  }
}
