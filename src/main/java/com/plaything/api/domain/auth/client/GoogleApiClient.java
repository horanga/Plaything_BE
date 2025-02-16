package com.plaything.api.domain.auth.client;

import com.plaything.api.domain.auth.client.dto.response.GoogleUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "googleApi", url = "https://www.googleapis.com")
public interface GoogleApiClient {

  @GetMapping("/oauth2/v3/userinfo")
  GoogleUserInfo getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
