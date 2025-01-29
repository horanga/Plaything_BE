package com.plaything.api.domain.admin.model.response;

import com.plaything.api.domain.profile.model.response.UserStats;
import java.util.List;

public record UserStatResponse(
    List<UserStats> list
) {

}
