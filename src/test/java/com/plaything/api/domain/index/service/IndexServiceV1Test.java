package com.plaything.api.domain.index.service;

import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.Message;
import com.plaything.api.domain.chat.service.ChatFacadeV1;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.plaything.api.domain.user.constants.Gender.M;

@SpringBootTest
class IndexServiceV1Test {

    @Autowired
    private IndexServiceV1 indexServiceV1;

    @Autowired
    private ChatFacadeV1 chatFacadeV1;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;


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

    }

    @DisplayName("메시지를 받으면 신규 메시지 표시가 뜬다")
    @Test
    void test1() {

        chatFacadeV1.saveMessage(new Message("alex", "alex2", "hi"), LocalDateTime.now());
        indexServiceV1.refreshIndex("dusgh1234");

    }

}