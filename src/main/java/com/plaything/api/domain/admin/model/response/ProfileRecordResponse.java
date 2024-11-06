package com.plaything.api.domain.admin.model.response;

import com.plaything.api.domain.repository.entity.monitor.ProfileRecord;
import com.plaything.api.domain.user.constants.ProfileStatus;
import com.plaything.api.domain.user.model.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User 프로필 변경 모니터링 데이터")
public record ProfileRecordResponse(
        @Schema(description = "모니터링 레코드 Id")
        long recordId,

        @Schema(description = "자기 소개")
        String introduction,

        @Schema(description = "닉네임")
        String nickName,

        @Schema(description = "프로필 상태")
        ProfileStatus status,

        @Schema(description = "프로필 id")
        long profileId,

        @Schema(description = "유저 정보")
        UserResponse user
) {
    public static ProfileRecordResponse toResponse(ProfileRecord profileRecord){
        return new ProfileRecordResponse(
                profileRecord.getId(),
                profileRecord.getIntroduction(),
                profileRecord.getNickName(),
                profileRecord.getProfileStatus(),
                profileRecord.getProfileId(),
                UserResponse.toResponse(profileRecord.getUser()));
    }
}