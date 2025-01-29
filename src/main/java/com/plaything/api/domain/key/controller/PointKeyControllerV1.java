package com.plaything.api.domain.key.controller;

import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKeyResponse;
import com.plaything.api.domain.key.model.response.PointKeyLogResponse;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Points", description = "V1 PointKey API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/point")
public class PointKeyControllerV1 {

    private final PointKeyFacadeV1 pointKeyFacadeV1;

    @Operation(
            summary = "광고 보상 포인트 키 생성",
            description = """
                    광고 시청 완료 후 포인트 키를 생성하고 지급합니다
                    
                    ## 주의
                    요청을 보낼 땐 중복 요청을 필터링하기 위해서
                    클라이언트에서 요청의 트랜잭션 ID를 난수로 생성해서 보내주어야 합니다.
                    """
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/create-key")
    public void createPointKey(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Transaction-ID") String transactionId,
            @Valid @RequestBody AdRewardRequest request
    ) {
        String user = userDetails.getUsername();
        pointKeyFacadeV1.createPointKeyForAd(user, request, LocalDateTime.now(), transactionId);
    }

    @Operation(
            summary = "포인트 키 사용 내역 조회",
            description = "사용자의 포인트 키 획득/사용 이력을 조회합니다"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-keylog")
    public PointKeyLogResponse getPointKeyLog(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String user = userDetails.getUsername();
        return new PointKeyLogResponse(pointKeyFacadeV1.getPointKeyLog(user));
    }


    @Operation(
            summary = "보유 포인트 키 수량 조회",
            description = "현재 사용 가능한 포인트 키의 수량을 조회합니다"
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/get-key")
    public AvailablePointKeyResponse getPointKey(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String user = userDetails.getUsername();
        return new AvailablePointKeyResponse(List.of(pointKeyFacadeV1.getAvailablePointKey(user)));
    }

}
