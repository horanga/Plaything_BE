package com.plaything.api.domain.chat.model.response;

import com.plaything.api.domain.repository.entity.user.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;

public record ChatProfile(
        @Schema(description = "채팅 내역")
        String mainPhoto,

        @Schema(description = "채팅 상대방 성향")
        String primaryRole,

        @Schema(description = "채팅 상대방 닉네임")
        String nickName
) {

    public static ChatProfile toResponse(Profile profile, String imageUrl) {
        return new ChatProfile(
                imageUrl,
                profile.getPrimaryRoleAsString(),
                profile.getNickName());

    }
}
