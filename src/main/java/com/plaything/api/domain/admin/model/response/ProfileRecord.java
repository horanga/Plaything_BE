package com.plaything.api.domain.admin.model.response;

import com.plaything.api.domain.profile.constants.ProfileStatus;
import com.plaything.api.domain.profile.model.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User 프로필 변경 모니터링 데이터")
public record ProfileRecord(
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
    public static ProfileRecord toResponse(com.plaything.api.domain.repository.entity.monitor.ProfileRecord profileRecord) {
        return new ProfileRecord(
                profileRecord.getId(),
                profileRecord.getIntroduction(),
                profileRecord.getNickName(),
                profileRecord.getProfileStatus(),
                profileRecord.getProfileId(),
                UserResponse.toResponse(profileRecord.getUser()));
    }
}