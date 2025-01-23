package com.plaything.api.domain.auth.client.dto.response;

public record ApplePublicKey(
        String kty,
        String kid,
        String alg,
        String n,
        String e) {
}
