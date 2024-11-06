package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User 리스트의 프로필")
public record UserStats(
        @Schema(description = "프로필 위반 수치")
        long bannedProfileCount,

        @Schema(description = "신고 관련 수치")
        long reportViolationCount,

        @Schema(description = "사진 위반 수치")
        long bannedImageCount

) {

    public static UserStats toResponse(UserViolationStats user){
        return new UserStats(
                user.getBannedProfileCount(),
                user.getReportViolationCount(),
                user.getBannedImageCount());
    }
}
