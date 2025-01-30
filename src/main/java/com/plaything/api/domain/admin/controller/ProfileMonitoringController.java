package com.plaything.api.domain.admin.controller;

import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.model.response.UserStatResponse;
import com.plaything.api.domain.admin.service.ProfileMonitoringFacadeV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Monitoring", description = "V1 Profile Monitoring API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/profile-monitorings")
public class ProfileMonitoringController {

  private final ProfileMonitoringFacadeV1 profileMonitoringFacadeV1;

  @Operation(
      summary = "Get all profile monitoring records",
      description = "유저 프로필 모니터링 정보 불러오기"
  )
  @GetMapping
  public ProfileRecordResponse getProfileRecords() {
    return new ProfileRecordResponse(profileMonitoringFacadeV1.getRecords());
  }

  @Operation(
      summary = "Get user stats",
      description = "유저 금지 행위 통계"
  )
  @GetMapping("/users/{userId}/stats")
  public UserStatResponse getUserStats(
      @PathVariable("userId") Long userId
  ) {
    return new UserStatResponse(List.of(profileMonitoringFacadeV1.getUserStats(userId)));
  }

  @Operation(
      summary = "Approve profile",
      description = "유저 프로필 승인하기"
  )
  @PutMapping("/users/{profileId}/approvals")
  public void approveProfile(@PathVariable("profileId") long profileId) {
    profileMonitoringFacadeV1.approveProfile(profileId);
  }

  @Operation(
      summary = "Approve profile",
      description = "유저 프로필 거절하기"
  )
  @PutMapping("/users/{profileId}/rejections")
  public void rejectProfile(@PathVariable("profileId") long profileId,
      @RequestBody String rejectReason) {
    profileMonitoringFacadeV1.rejectProfile(profileId, rejectReason);
  }
}
