package com.plaything.api.domain.index.controller;

import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.index.service.IndexServiceV1;
import com.plaything.api.security.JWTProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class IndexController {

    private final IndexServiceV1 indexServiceV1;

    @Operation(
            summary = "새로운 메시지 여부를 확인",
            description = "새로운 메시지가 있는지 확인하는 refresh Index API"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping
    public IndexResponse refreshIndex(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authString
    ) {

        //TODO 웹소켓 연결 끊겼을 때로 변경
        String token = JWTProvider.extractToken(authString);
        String user = JWTProvider.getUserFromToken(token);
        return indexServiceV1.refreshIndex(user);
    }
}
