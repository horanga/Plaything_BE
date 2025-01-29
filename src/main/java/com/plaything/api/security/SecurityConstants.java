package com.plaything.api.security;

public class SecurityConstants {

  private static final String[] AUTH_WHITELIST_VALUES = {
      "/api/v1/auth/create-user",
      "/api/v1/auth/login",
      "/swagger-ui/**",
      "/v3/**",
      "/swagger-resources/**",
      "/api/v1/auth/google",
      "/api/v1/auth/google/login",
      "/api/v1/auth/apple/login",
      "/ws-stomp/**"
  };

  public static String[] getAuthWhitelist() {
    return AUTH_WHITELIST_VALUES.clone();
  }
}
