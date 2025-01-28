package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.model.response.MyPageProfile;
import com.plaything.api.domain.repository.repo.monitor.ProfileRecordRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.util.UserGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.plaything.api.domain.profile.constants.Gender.F;
import static com.plaything.api.domain.profile.constants.Gender.M;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.*;
import static com.plaything.api.domain.profile.constants.PrimaryRole.BOTTOM;
import static com.plaything.api.domain.profile.constants.PrimaryRole.TOP;
import static com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
public class ProfileRollbackTest {

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRecordRepository profileRecordRepository;

    @AfterEach
    void tearDown() {
        profileRecordRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        TestTransaction.flagForCommit();
        TestTransaction.end();

    }


    @DisplayName("프로필을 업데이트할 때 이미 존재하는 닉네임으로 변경할 수 없다.")
    @Test
    void test1() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");


        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이",
                F,
                "안녕하세요",
                BOTTOM,
                List.of(0),
                List.of(BRAT_TAMER),
                null,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 등록된 닉네임입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.primaryRole()).isEqualTo("MT");
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKER, HUNTER);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);


    }

    @DisplayName("프로필을 업데이트할 때 대표성향이 없으면 업데이트에 실패한다")
    @Test
    void test2() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                TOP,
                List.of(1),
                List.of(BRAT_TAMER),
                null,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향을 선택하지 않았습니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.primaryRole()).isEqualTo("MT");
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKER, HUNTER);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향과 다른 상세성향을 선택하면 프로필 업데이트가 실패한다(탑->바텀)")
    @Test
    void test3() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                TOP,
                List.of(1),
                List.of(SPANKEE),
                null,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향과 일치하지 않는 세부성향입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.primaryRole()).isEqualTo("MT");
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKER, HUNTER);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향과 다른 상세성향을 선택하면 프로필 업데이트가 실패한다(바텀->탑)")
    @Test
    void test4() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                BOTTOM,
                List.of(SPANKEE),
                SPANKEE,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                BOTTOM,
                List.of(0),
                List.of(SPANKER),
                SPANKER,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향과 일치하지 않는 세부성향입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKEE);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향과 다른 상세성향을 선택하면 프로필 업데이트가 실패한다2(탑)")
    @Test
    void test5() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                BOTTOM,
                List.of(1),
                List.of(SPANKEE),
                null,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향과 일치하지 않는 세부성향입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.primaryRole()).isEqualTo("MT");
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKER, HUNTER);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향과 다른 상세성향을 선택하면 프로필 업데이트가 실패한다2(바텀)")
    @Test
    void test6() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                BOTTOM,
                List.of(SPANKEE, ROPE_BUNNY),
                SPANKEE,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                TOP,
                List.of(0),
                List.of(SPANKER),
                null,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향과 일치하지 않는 세부성향입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKEE, ROPE_BUNNY);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향과 다른 상세성향을 선택하면 프로필 업데이트가 실패한다3")
    @Test
    void test7() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                BOTTOM,
                List.of(1),
                Collections.emptyList(),
                null,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향과 일치하지 않는 세부성향입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.primaryRole()).isEqualTo("MT");
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKER, HUNTER);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향과 다른 상세성향을 선택하면 프로필 업데이트가 실패한다3")
    @Test
    void test8() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                BOTTOM,
                List.of(SPANKEE, ROPE_BUNNY),
                SPANKEE,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                TOP,
                List.of(0),
                Collections.emptyList(),
                ROPE_BUNNY,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향과 일치하지 않는 세부성향입니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKEE, ROPE_BUNNY);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향을 두개 이상 선택하면 프로필 업데이트에 실패한다(탑)")
    @Test
    void test9() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                TOP,
                Collections.emptyList(),
                List.of(DOMINANT),
                DOMINANT,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향은 두 개 이상일 수 없습니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(SPANKER, HUNTER);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }

    @DisplayName("대표성향을 두개 이상 선택하면 프로필 업데이트에 실패한다(바텀)")
    @Test
    void test10() {

        userGenerator.generate("fnel1234", "123", "d", "연돌이");
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                BOTTOM,
                List.of(BRAT),
                BRAT,
                List.of(RelationshipPreferenceConstant.DATE_DS, DS),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "fnel123");
        ProfileUpdate profileUpdate = new ProfileUpdate(
                "연돌이D",
                M,
                "안녕하세요",
                BOTTOM,
                Collections.emptyList(),
                List.of(DEGRADEE),
                DEGRADEE,
                List.of(0),
                List.of(HOM)
        );

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        assertThatThrownBy(() -> profileFacadeV1.updateProfile(profileUpdate, "fnel123"))
                .isInstanceOf(CustomException.class)
                .hasMessage("대표성향은 두 개 이상일 수 없습니다");

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        MyPageProfile profile = profileFacadeV1.getMyPageProfile("fnel123");
        assertThat(profile.nickName()).isEqualTo("알렉1");
        assertThat(profile.gender()).isEqualTo(M);
        assertThat(profile.introduction()).isEqualTo("안녕하세요");
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(BRAT);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(true);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(DATE_DS, DS);
    }
}
