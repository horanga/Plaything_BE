package com.plaything.api.domain.key.controller;

import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.key.model.response.PointKeyLog;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Point Key API", description = "V1 PointKey API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/point")
public class PointKeyControllerV1 {

    private final PointKeyFacadeV1 pointKeyFacadeV1;

    @Operation(
            summary = "Get pointkey",
            description = "광고 시청 후 포인트 키 받기"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/create-key")
    public void createPointKey(
            @RequestHeader(value = "Authorization", required = false) String authString,
            @RequestHeader("Transaction-ID") String transactionId,
            @Valid @RequestBody AdRewardRequest request
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        pointKeyFacadeV1.createPointKeyForAd(user, request, LocalDateTime.now(), transactionId);
    }

    @Operation(
            summary = "Get pointkey log",
            description = "포인트 키 로그 확인"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-keylog")
    public List<PointKeyLog> getPointKeyLog(
            @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return pointKeyFacadeV1.getPointKeyLog(user);
    }


    @Operation(
            summary = "Get pointkey",
            description = "포인트 키 개수 확인"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-key")
    public AvailablePointKey getPointKey(
            @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return pointKeyFacadeV1.getAvailablePointKey(user);
    }

    @Operation(
            summary = "use pointkey",
            description = "매칭을 위한 키 사용"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/use-key")
    public void usePointKey(
            @RequestHeader(value = "Authorization", required = false) String authString,
            @RequestBody MatchingRequest matchingRequest,
            @RequestHeader("Transaction-ID") String transactionId
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        pointKeyFacadeV1.usePointKeyForMatching(user, matchingRequest, transactionId);
    }
}
