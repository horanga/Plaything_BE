package com.plaything.api.domain.auth.client;

import com.plaything.api.domain.auth.client.dto.response.ApplePublicKeyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "appleApi", url = "https://appleid.apple.com")
public interface AppleApiClient {

  @GetMapping("/auth/keys")
  ApplePublicKeyResponse getPublicKeys();
}
