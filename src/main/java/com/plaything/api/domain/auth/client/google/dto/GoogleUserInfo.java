package com.plaything.api.domain.auth.client.google.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserInfo {
    private String sub;
    private String provider;

    public void setProvider() {
        this.provider = "Google";
    }
}
