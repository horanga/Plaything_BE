package com.plaything.api.domain.matching.controller;


import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.matching.service.MatchingServiceV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Matching API", description = "V1 Matching API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingControllerV1 {

    private final MatchingServiceV1 matchingServiceV1;

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