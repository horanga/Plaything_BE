package com.plaything.api.domain.auth.client.dto.response;

import org.apache.coyote.BadRequestException;

import java.util.List;

public record ApplePublicKeyResponse(List<ApplePublicKey> keys) {
    public ApplePublicKey getMatchedKey(String kid, String alg) throws BadRequestException {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findAny()
                .orElseThrow(BadRequestException::new);
    }
}
