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
            summary = "매칭 요청",
            description = """
                    매칭 서비스 이용을 위해 포인트 키를 사용합니다.
                    매칭 대상의 로그인 ID를 request에 담아서 요청을 보냅니다.
                    
                    #예외#
                    (1) 네트워크 에러로 중복 요청이 들어오면 '이미 처리된 요청(conflict)' 예외
                    (2) 매칭에 필요한 재화가 없는 경우 예외 발생(not_found)
                    (3) 알림 전송 실패 시 예외(server-error)
                    """
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
            summary = "매칭 요청 수락",
            description = """
                    매칭 요구를 수락하기 위해 포인트 키를 사용합니다.
                    이 api는 매칭 요청이 온 경우에만 사용 가능합니다.
                    
                    #예외#
                    (1) 네트워크 에러로 중복 요청이 들어오면 '이미 처리된 요청(conflict)' 예외
                    (2) 매칭에 필요한 재화가 없는 경우 예외 발생(not_found)
                    (3) 매칭 요청이 없었던 경우 예외 발생(not_found)
                    """
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
            summary = "매칭 가능한 프로필 조회",
            description = """
                    이용자 성향에 맞춰 매칭 가능한 프로필 목록을 조회합니다.
                    매칭 시 프로필에 있는 로그인 id를 활용할 수 있습니다.
                    """
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