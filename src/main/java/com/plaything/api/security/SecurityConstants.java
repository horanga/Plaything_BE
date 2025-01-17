package com.plaything.api.security;

public class SecurityConstants {

    private static final String[] AUTH_WHITELIST_VALUES = {
            "/api/v1/auth/create-user",
            "/api/v1/auth/login",
            "/swagger-ui/**",
            "/v3/**",
            "/swagger-resources/**"
    };

    public static String[] getAuthWhitelist() {
        return AUTH_WHITELIST_VALUES.clone();
    }
}
