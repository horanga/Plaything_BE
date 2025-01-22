package com.plaything.api.domain.auth.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppleUserInfo {

    private String sub;
    private String provider;

    public void setProvider() {
        this.provider = "Apple";
    }
}
