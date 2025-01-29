package com.plaything.api.domain.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User 프로필 변경 모니터링 데이터")
public record ProfileRecordResponse(
        @Schema(description = "모니터링 레코드 list")
        List<ProfileRecord> list
) {
}
