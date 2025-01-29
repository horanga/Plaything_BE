package com.plaything.api.domain.matching.controller;


import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.matching.model.response.UserMatchingResponse;
import com.plaything.api.domain.matching.service.MatchingFacadeV1;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.plaything.api.domain.matching.constants.MatchingConstants.*;

@Tag(name = "Matchings", description = "V1 매칭 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching")
public class MatchingControllerV1 {

    private final MatchingFacadeV1 matchingFacadeV1;
    private final ProfileFacadeV1 profileFacadeV1;

    @Operation(
            summary = "매칭 요청",
            description = """
                    매칭 서비스 이용을 위해 포인트 키를 사용합니다.
                    매칭 대상의 로그인 ID를 request에 담아서 요청을 보냅니다.
                    
                    ## 주의
                    요청을 보낼 땐 중복 요청을 필터링하기 위해서
                    클라이언트에서 요청의 트랜잭션 ID를 난수로 생성해서 보내주어야 합니다.
                    
                    #요청값-바디#
                    (1)MatchingRequest 객체: 매칭을 신청하려는 이용자의 로그인 id
                    
                    (2)Transaction-ID: 요청의 ID 값(중복 요청을 거르는 난수)
                    
                    #응답#
                    성공: 200 OK
                    
                    #예외#
                    (1) 409 Conflict
                    - 네트워크 에러로 인한 중복 요청
                    
                    (2) 404 NOT FOUND
                    - 매칭에 필요한 재화 부족
                    
                    (3) 500 Server-error
                    - 알림 전송 실패
                    """
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/create-matching")
    public void createMatching(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MatchingRequest matchingRequest,
            @RequestHeader("Transaction-ID") String transactionId
    ) {
        String user = userDetails.getUsername();
        matchingFacadeV1.sendMatchingRequest(user, matchingRequest, transactionId);
    }

    @Operation(
            summary = "매칭 요청 수락",
            description = """
                    매칭 요구를 수락하기 위해 포인트 키를 사용합니다.
                    이 api는 매칭 요청이 온 경우에만 사용 가능합니다.
                    
                    ## 주의
                    요청을 보낼 땐 중복 요청을 필터링하기 위해서
                    클라이언트에서 요청의 트랜잭션 ID를 난수로 생성해서 보내주어야 합니다.
                    
                    #요청값-바디#
                    
                    (1)MatchingRequest 객체: 매칭을 신청하려는 이용자의 로그인 id
                    
                    (2)Transaction-ID: 요청의 ID 값(중복 요청을 거르는 난수)
                    
                    #응답#
                    성공: 200 OK
                    
                    #예외#
                    
                    (1) 409 Conflict
                    - 네트워크 에러로 인한 중복 요청
                    
                    (2) 404 NOT FOUND
                    - 매칭에 필요한 재화 부족
                    - 수락하려는 매칭 요청이 존재하지 않음
                    
                    (4) 401 Unauthorized
                    - 인증되지 않은 사용자
                    """
    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/accept-matching")
    public void acceptMatching(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MatchingRequest matchingRequest,
            @RequestHeader("Transaction-ID") String transactionId
    ) {
        String user = userDetails.getUsername();
        matchingFacadeV1.acceptMatchingRequest(user, matchingRequest, transactionId);
    }

    @Operation(
            summary = "매칭 가능한 프로필 조회",
            description = """
                    이용자 성향에 맞춰 매칭 가능한 프로필 목록을 조회합니다.
                    매칭 시 프로필에 있는 로그인 id를 활용할 수 있습니다.
                    
                    #매칭 목록에서 제외되는 대상
                    (1) 매칭을 신청한 사람
                    (2) 매칭이 성사된 사람
                    (3) 프로필 숨기기를 한 사람
                    
                    위 세 대상들은 캐싱을 하기 때문에 실시간 반영이 안 될 수 있습니다.
                    
                    #응답#
                    성공: 200 OK
                    """
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping
    public UserMatchingResponse matching(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String user = userDetails.getUsername();
        return new UserMatchingResponse(
                matchingFacadeV1.findMatchingCandidates(user, CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS));
    }

    @Operation(
            summary = "매칭 프로필 넘어가기(SKIP)",
            description = """
                    매칭 프로필 리스트에서 '넘어가기(SKIP)'할 때 이 API를 호출합니다.
                    마지막으로 조회한 프로필 ID를 저장해 다음 매칭 프로필 ID 조회에 활용합니다.
                    
                    #요청값-URL#
                    (1)lastProfileId : 마지막으로 조회(skip)한 프로필의 id
                    
                    
                    """
    )
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/profile-skip")
    public void recordProfileSkip(
            @AuthenticationPrincipal UserDetails userDetails
            , @RequestParam(value = "lastProfileId") long lastProfileId
    ) {
        String user = userDetails.getUsername();
        matchingFacadeV1.updateLastViewedProfile(
                user,
                lastProfileId,
                EXPIRATION_DATE_SKIP_COUNT,
                EXPIRATION_DATE_PROFILE_ID,
                CACHE_DURATION_UNIT_DAYS);
    }

    @Operation(
            summary = "특정 이용자 프로필을 매칭에서 차단",
            description = """
                    매칭 조회 시 특정 이용자의 프로필이 일주일간 포함되지 않도록 설정합니다
                    """

    )
    @SecurityRequirement(name = "Authorization")
    @PostMapping(value = "/hide-profile/{loginId}")
    public void hideProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "loginId") String loginId) {
        String user = userDetails.getUsername();
        profileFacadeV1.hideProfile(user, loginId, LocalDate.now());
    }
}