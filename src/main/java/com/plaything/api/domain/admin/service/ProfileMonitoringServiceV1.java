package com.plaything.api.domain.admin.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.repository.entity.monitor.ProfileRecord;
import com.plaything.api.domain.repository.entity.monitor.RejectedProfile;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.monitor.ProfileRecordRepository;
import com.plaything.api.domain.repository.repo.monitor.RejectedProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProfileMonitoringServiceV1 {

    private final ProfileRecordRepository profileRecordRepository;

    private final RejectedProfileRepository rejectedProfileRepository;

    public void saveProfileRecord(
            String introduction,
            String nickname,
            long id,
            ProfileStatus status,
            User user) {
        ProfileRecord record = ProfileRecord.builder()
                .introduction(introduction)
                .nickName(nickname)
                .user(user)
                .profileId(id)
                .profileStatus(status)
                .build();
        profileRecordRepository.save(record);
    }

    public List<ProfileRecordResponse> getRecords() {
        //TODO 노오프셋 페이지네이션으로 변경

        return profileRecordRepository.findAll()
                .stream().map(ProfileRecordResponse::toResponse).toList();
    }

    public ProfileRecord findByRecordId(Long recordId) {
        return profileRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_PROFILE_RECORD));
    }

    public void deleteById(Long id) {
        profileRecordRepository.deleteById(id);
    }

    public void saveRejectedProfile(Profile profile, User user, String rejectedReason) {
        RejectedProfile rejectedProfile = RejectedProfile.builder()
                .rejectedReason(rejectedReason)
                .profileId(profile.getId())
                .nickName(profile.getNickName())
                .introduction(profile.getIntroduction())
                .user(user).build();

        rejectedProfileRepository.save(rejectedProfile);
    }
}
