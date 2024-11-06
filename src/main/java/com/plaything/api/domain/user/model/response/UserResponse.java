package com.plaything.api.domain.user.model.response;

import com.plaything.api.domain.repository.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User 리스트의 프로필")
public record UserResponse(
        @Schema(description = "user의 id")
        long userId,

        @Schema(description = "로그인 id")
        String name
) {

    public static UserResponse toResponse(User user){
        return new UserResponse(user.getId(), user.getName());
    }
}
