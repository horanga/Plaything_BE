package com.plaything.api.domain.profile.model.response;

import java.util.List;

public record MyPageProfileResponse(
        List<MyPageProfile> list
) {
}
