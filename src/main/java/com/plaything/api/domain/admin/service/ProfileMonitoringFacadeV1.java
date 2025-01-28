package com.plaything.api.domain.admin.service;

import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.profile.model.response.UserStats;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import com.plaything.api.domain.profile.service.UserServiceV1;
import com.plaything.api.domain.repository.entity.monitor.ProfileRecord;
import com.plaything.api.domain.repository.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileMonitoringFacadeV1 {

    private final UserServiceV1 userServiceV1;
    private final ProfileMonitoringServiceV1 profileMonitoringServiceV1;
    private final ProfileFacadeV1 profileFacadeV1;

    public List<ProfileRecordResponse> getRecords() {
        return profileMonitoringServiceV1.getRecords();
    }

    public void approveProfile(Long recordId) {
        profileMonitoringServiceV1.deleteById(recordId);
    }

    @Transactional
    public void rejectProfile(Long recordId, String rejectedReason) {
        ProfileRecord record = profileMonitoringServiceV1.findByRecordId(recordId);
        profileFacadeV1.banProfile(record.getProfileId(), rejectedReason, record.getUser());
        profileMonitoringServiceV1.deleteById(recordId);
    }

    public UserStats getUserStats(Long id) {
        User user = userServiceV1.findById(id);
        return UserStats.toResponse(user.getViolationStats());
    }
}
