package com.plaything.api.domain.matching.model.request;

import com.plaything.api.domain.profile.constants.PrimaryRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이용자 매칭 료청")
public record MatchRequestForOthers(

        @Schema(description = "대표성향의 그룹")
        PrimaryRole primaryRole,

        @Schema(description = "마지막으로 받은 유저의 id")
        long lastId,

        @Schema(description = "이용자 닉네임")
        String userName

) {

    public static MatchRequestForOthers from(
            PrimaryRole primaryRole,
            long lastId,
            String userName) {
        return new MatchRequestForOthers(primaryRole, lastId, userName);
    }

}