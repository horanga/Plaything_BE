package com.plaything.api.domain.matching.controller;


import com.plaything.api.domain.matching.service.MatchingServiceV1;
import com.plaything.api.domain.user.model.response.UserMatching;
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

    @Operation(
            summary = "Matching start",
            description = "다른 유저와 매칭"
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