package com.plaything.api.domain.chat.interceptor;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.response.ProfileResponse;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.UserServiceV1;
import com.plaything.api.security.JWTProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.plaything.api.domain.user.constants.Gender.M;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class SessionValidatorTest {

    @Autowired
    private SessionValidator sessionValidator;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private UserServiceV1 userServiceV1;


    @BeforeEach
    void setup() {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "alex", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);

        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "alex2", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        CreateUserRequest request3 = new CreateUserRequest("dusgh12346", "1234", "1");
        authServiceV1.creatUser(request3);

        Matching matching = Matching.builder().receiverNickname("alex").senderNickname("alex2").isMatched(true).isOvered(false).build();
        matchingRepository.save(matching);
    }

    @AfterEach
    void cleanData() {
        sessionValidator.clean();
    }


    @DisplayName("정상적인 jwt 토큰이 들어오면 connect 검증이 된다.")
    @Test
    void test1() {

        String loginId = "dusgh1234";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + token, sessionId);
    }

    @DisplayName("존재하지 않는 회원의 jwt 토큰이 들어오면 connect 검증이 실패한다")
    @Test
    void test2() {

        String loginId = "dusghd";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";
        assertThatThrownBy(() -> sessionValidator.validateConnect("Bearer " + token, sessionId))
                .isInstanceOf(CustomException.class).hasMessage("존재하지 않는 회원입니다");
    }

    @DisplayName("프로필이 없는 회원의 jwt 토큰이 들어오면 connect 검증이 실패한다")
    @Test
    void test3() {

        String loginId = "dusgh12346";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";
        assertThatThrownBy(() -> sessionValidator.validateConnect("Bearer " + token, sessionId))
                .isInstanceOf(CustomException.class).hasMessage("존재하지 않는 회원입니다");
    }

    @DisplayName("프로필을 차단당한 회원의 jwt 토큰이 들어오면 connect 검증이 실패한다")
    @Test
    void test4() {

        String loginId = "dusgh1234";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";

        ProfileResponse profileByLoginId = profileFacadeV1.getProfileByLoginId("dusgh1234");
        User user = userServiceV1.findByLoginId("dusgh1234");

        profileFacadeV1.banProfile(profileByLoginId.id(), "부적절한 사진", user);

        assertThatThrownBy(() -> sessionValidator.validateConnect("Bearer " + token, sessionId))
                .isInstanceOf(CustomException.class).hasMessage("부적절한 프로필로 비활성화된 회원입니다");
    }

    @DisplayName("자기 채널에만 구독을 할 수 있다")
    @Test
    void test5() {

        String loginId = "dusgh1234";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";
        String destination = "/user/alex/chat";

        sessionValidator.validateConnect("Bearer " + token, sessionId);
        sessionValidator.validateSubscribe(sessionId, destination);
    }

    @DisplayName("자신의 것이 아닌 다른 채널을 구독하려면 실패한다")
    @Test
    void test6() {

        String loginId = "dusgh1234";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";
        String destination2 = "/user/alex2/chat";
        String destination3 = "/user/chat";
        String destination4 = "/user";
        String destination5 = "/chat";
        String destination6 = "/";
        String destination7 = "/user/ /chat";
        String destination8 = "/sub";
        String destination9 = "/user//chat";

        sessionValidator.validateConnect("Bearer " + token, sessionId);

        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination2))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination3))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination4))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination5))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination6))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination7))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination8))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination9))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
    }

    @DisplayName("다른 세션의 채널을 구독하려면 실패한다")
    @Test
    void test7() {

        String loginId = "dusgh1234";
        String token = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + token, sessionId);

        String loginId2 = "dusgh12345";
        String token2 = JWTProvider.createToken(loginId2);
        String sessionId2 = "session2";

        sessionValidator.validateConnect("Bearer " + token2, sessionId2);

        String destination = "/user/alex/chat";

        sessionValidator.validateSubscribe(sessionId, destination);

        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId2, destination))
                .isInstanceOf(CustomException.class).hasMessage("채널 구독 권한이 없습니다");
    }

    @DisplayName("매칭된 상대방에게 메시지를 보낼 수 있다")
    @Test
    void test8() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + alex, sessionId);

        String loginId2 = "dusgh12345";
        String alex2 = JWTProvider.createToken(loginId2);
        String sessionId2 = "session2";

        sessionValidator.validateConnect("Bearer " + alex2, sessionId2);

        String destination = "/user/alex/chat";
        String destination2 = "/pub/chat/chat/alex";

        sessionValidator.validateSubscribe(sessionId, destination);

        sessionValidator.validateSend("Bearer " + alex2, sessionId2, destination2);

    }

    @DisplayName("매칭되지 않은 상대방에게 메시지를 보낼 수 없다")
    @Test
    void test9() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + alex, sessionId);

        String loginId2 = "dusgh12346";
        String alex2 = JWTProvider.createToken(loginId2);
        String sessionId2 = "session2";
        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "alex3", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.now());

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12346");

        sessionValidator.validateConnect("Bearer " + alex2, sessionId2);

        String destination = "/user/alex/chat";

        sessionValidator.validateSubscribe(sessionId, destination);

        String destination2 = "/pub/chat/chat/alex";
        assertThatThrownBy(() ->
                sessionValidator.validateSend("Bearer " + alex2, sessionId2, destination2))
                .isInstanceOf(CustomException.class).hasMessage("매칭 파트너가 없습니다");


        Matching matching = Matching.builder().receiverNickname("alex2").senderNickname("alex3").isMatched(true).isOvered(false).build();
        matchingRepository.save(matching);

        assertThatThrownBy(() ->
                sessionValidator.validateSend("Bearer " + alex2, sessionId2, destination2))
                .isInstanceOf(CustomException.class).hasMessage("매칭된 파트너가 아닙니다");
    }

    @DisplayName("세션에 등록된 id와 다르면 상대방에게 메시지를 보낼 수 없다")
    @Test
    void test10() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + alex, sessionId);

        String loginId2 = "dusgh12346";
        String alex2 = JWTProvider.createToken(loginId2);
        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "alex3", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.now());

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12346");
        String destination = "/pub/chat/chat/alex2";
        assertThatThrownBy(() -> sessionValidator.validateSend("Bearer " + alex2, sessionId, destination))
                .isInstanceOf(CustomException.class).hasMessage("메시지 발신자가 세션 회원과 일치하지 않습니다");
    }

    @DisplayName("소켓 연결을 종료할 수 있다")
    @Test
    void test11() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + alex, sessionId);
        sessionValidator.processDisconnect(sessionId);
    }

    @DisplayName("소켓 연결을 종료한 후 구독, 메시지 전송을 할 수 없다.")
    @Test
    void test14() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";

        sessionValidator.validateConnect("Bearer " + alex, sessionId);
        sessionValidator.processDisconnect(sessionId);

        String destination = "/user/alex/chat";
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination))
                .isInstanceOf(CustomException.class).hasMessage("STOMP에 채팅 연결이 된 회원이 아닙니다");

        String destination2 = "/pub/chat/chat/alex2";
        assertThatThrownBy(() -> sessionValidator.validateSend("Bearer " + alex, sessionId, destination2))
                .isInstanceOf(CustomException.class).hasMessage("STOMP에 채팅 연결이 된 회원이 아닙니다");
    }

    @DisplayName("연결을 하지 않고 메시지를 보낼 수 없다")
    @Test
    void test12() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";
        String destination = "/pub/chat/chat/alex2";
        assertThatThrownBy(() -> sessionValidator.validateSend("Bearer " + alex, sessionId, destination))
                .isInstanceOf(CustomException.class).hasMessage("STOMP에 채팅 연결이 된 회원이 아닙니다");
    }

    @DisplayName("연결을 하지 않고 구독을할 수 없다.")
    @Test
    void test13() {

        String loginId = "dusgh1234";
        String alex = JWTProvider.createToken(loginId);
        String sessionId = "session";
        String destination = "/user/alex/chat";
        assertThatThrownBy(() -> sessionValidator.validateSubscribe(sessionId, destination))
                .isInstanceOf(CustomException.class).hasMessage("STOMP에 채팅 연결이 된 회원이 아닙니다");
    }


}