package com.plaything.api.domain.index.controller;

import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.index.service.IndexServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class IndexController {

    private final IndexServiceV1 indexServiceV1;

    @Operation(
            summary = "새로운 메시지 여부를 확인",
            description = "새로운 메시지가 있는지 확인하는 용도입니다"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping
    public IndexResponse refreshIndex(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        //TODO 웹소켓 연결 끊겼을 때로 변경
        String user = userDetails.getUsername();
        return indexServiceV1.refreshIndex(user);
    }
}
