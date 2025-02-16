package com.plaything.api.domain.repository.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class UserViolationStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private long bannedProfileCount;      // 프로필 위반으로 인한 제재 횟수

  private long reportViolationCount;    // 신고 관련 위반 횟수

  private long bannedImageCount;        // 이미지 위반으로 인한 제재 횟수

  public void increaseBannedImageCount() {
    this.bannedImageCount++;
  }

  public void increaseReportViolationCount() {
    this.reportViolationCount++;
  }

  public void increaseBannedProfileCount() {
    this.bannedProfileCount++;
  }
}
