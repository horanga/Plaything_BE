package com.plaything.api.domain.matching.controller;


import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.matching.service.MatchingFacadeV1;
import com.plaything.api.domain.matching.service.MatchingServiceV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Matching API", description = "V1 Matching API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingControllerV1 {

    private final MatchingServiceV1 matchingServiceV1;
    private final MatchingFacadeV1 matchingFacadeV1;

    @Operation(
            summary = "매칭용 포인트 키 사용",
            description = "매칭 서비스 이용을 위해 포인트 키를 사용합니다"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/create-matching")
    public void createMatching(
            @RequestHeader(value = "Authorization", required = false) String authString,
            @RequestBody MatchingRequest matchingRequest,
            @RequestHeader("Transaction-ID") String transactionId
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        matchingFacadeV1.createMatching(user, matchingRequest, transactionId);
    }

    @Operation(
            summary = "매칭용 수락",
            description = "매칭 요구를 수락하기 위해 포인트 키를 사용합니다"
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/accpet-matching")
    public void acceptMatching(
            @RequestHeader(value = "Authorization", required = false) String authString,
            @RequestBody MatchingRequest matchingRequest,
            @RequestHeader("Transaction-ID") String transactionId
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        matchingFacadeV1.acceptMatching(user, matchingRequest, transactionId);
    }

    @Operation(
            summary = "Get matching partner list",
            description = "매칭되는 리스트 가져오기"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping
    public List<UserMatching> matching(
            @RequestHeader(value = "Authorization", required = false) String authString
    ) {
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return matchingServiceV1.match(user, 0L);
    }

}