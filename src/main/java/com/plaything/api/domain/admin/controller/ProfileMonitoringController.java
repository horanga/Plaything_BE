package com.plaything.api.domain.admin.controller;

import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringFacadeV1;
import com.plaything.api.domain.profile.model.response.UserStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Monitorings", description = "V1 Profile Monitoring API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/profiles")
public class ProfileMonitoringController {

    private final ProfileMonitoringFacadeV1 profileMonitoringFacadeV1;

    @Operation(
            summary = "Get all profile monitoring records",
            description = "유저 프로필 모니터링 정보 불러오기"
    )
    @GetMapping
    public List<ProfileRecordResponse> getProfileRecords() {
        return profileMonitoringFacadeV1.getRecords();
    }

    @Operation(
            summary = "Get user stats",
            description = "유저 금지 행위 통계"
    )
    @GetMapping("/{id}")
    public UserStats getUserStats(
            @PathVariable("id") Long id
    ) {
        return profileMonitoringFacadeV1.getUserStats(id);
    }

    @Operation(
            summary = "Approve profile",
            description = "유저 프로필 승인하기"
    )
    @DeleteMapping("/{id}")
    public void approveProfile(@PathVariable("id") long id) {
        profileMonitoringFacadeV1.approveProfile(id);
    }

    @Operation(
            summary = "Approve profile",
            description = "유저 프로필 거절하기"
    )
    @PutMapping("/{id}")
    public void rejectProfile(@PathVariable("id") long id,
                              @RequestBody String rejectReason) {
        profileMonitoringFacadeV1.rejectProfile(id, rejectReason);
    }
}
