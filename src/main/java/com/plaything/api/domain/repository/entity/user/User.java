package com.plaything.api.domain.repository.entity.user;

import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.profile.constants.Role;
import com.plaything.api.domain.repository.entity.common.BaseEntity;
import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import com.plaything.api.domain.repository.entity.profile.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String loginId;

  @Column(nullable = false)
  private String fcmToken;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column
  private boolean isDeleted;

  @OneToOne(cascade = {CascadeType.PERSIST,
      CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "User_credentials_id", referencedColumnName = "id")
  private UserCredentials credentials;

  @OneToOne(cascade = {CascadeType.PERSIST,
      CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "profile_id", referencedColumnName = "id")
  private Profile profile;

  @OneToOne(cascade = {CascadeType.PERSIST,
      CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "violation_stats_id", referencedColumnName = "id")
  private UserViolationStats violationStats;

  @OneToOne(cascade = {CascadeType.PERSIST,
      CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "user_reward_activity_id", referencedColumnName = "id")
  private UserRewardActivity userRewardActivity;

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public void setCredentials(UserCredentials credentials) {
    this.credentials = credentials;
  }

  public void setViolationStats(UserViolationStats violationStats) {
    this.violationStats = violationStats;
  }

  public void setUserRewardActivity(UserRewardActivity userRewardActivity) {
    this.userRewardActivity = userRewardActivity;
  }

  public boolean isProfileEmpty() {
    return this.profile == null;
  }

  public boolean isPreviousProfileRejected() {
    return this.profile.getProfileStatus().equals(ProfileStatus.REJECTED);
  }

  public String getNickname() {
    return this.profile.getNickName();
  }

  public void updateFcmToken(String fcmToken) {
    this.fcmToken = fcmToken;
  }

  public void delete() {
    this.isDeleted = true;
    profile.delete();
  }
}
