package com.plaything.api.domain.matching.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringFacadeV1;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.user.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.constants.Role;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.response.UserStats;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static com.plaything.api.domain.user.constants.Gender.M;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.*;
import static com.plaything.api.domain.user.constants.PrimaryRole.*;
import static com.plaything.api.domain.user.constants.RelationshipPreferenceConstant.MARRIAGE_DS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class MatchingServiceV1Test {

    @Autowired
    private MatchingServiceV1 matchingServiceV1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    private ProfileMonitoringFacadeV1 profileMonitoringFacadeV1;

    User user;

    User user2;

    User user3;

    User user4;

    @BeforeEach
    void setUp() {
        user = User.builder().name("fnel123").role(Role.ROLE_USER).build();
        userRepository.save(user);
        user2 = User.builder().name("fnel1234").role(Role.ROLE_USER).build();
        userRepository.save(user2);
        user3 = User.builder().name("fnel12344").role(Role.ROLE_USER).build();
        userRepository.save(user3);
        user4 = User.builder().name("fnel12345").role(Role.ROLE_USER).build();
        userRepository.save(user4);
    }

    @DisplayName("이용자는 대표성향의 반대 이용자와 매칭된다.(마조히스트))")
    @Test
    void test1() {

        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));


        registerProfile(
                "fnel12345",
                "알렉13",
                "잘부탁",
                TOP,
                List.of(DEGRADER),
                DEGRADER,
                List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user2);
        addImage(user3);
        addImage(user4);


        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).hasSize(2);
        assertThat(matched).extracting("primaryRole").containsExactly(BOTTOM, BOTTOM);
        assertThat(matched).extracting("nickName").containsExactly("알렉12", "알렉124");
        assertThat(matched).extracting("introduction").containsExactly("잘부탁", "잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(matched.get(1).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched.get(1).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("이용자는 대표성향의 반대 이용자와 매칭된다.(사디스트)")
    @Test
    void test2() {

        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(DEGRADER, SADIST),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));


        registerProfile(
                "fnel12345",
                "알렉13",
                "잘부탁",
                TOP,
                List.of(DEGRADER),
                DEGRADER,
                List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user2);
        addImage(user3);
        addImage(user4);

        List<UserMatching> matched = matchingServiceV1.match("fnel1234", 0L);
        assertThat(matched).extracting("primaryRole").containsExactly(TOP);
        assertThat(matched).extracting("nickName").containsExactly("알렉1");
        assertThat(matched).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(DEGRADER, SADIST);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(false, true);
        assertThat(matched.get(0).relationshipPreferenceList()).extracting("relationshipPreference").containsExactly(MARRIAGE_DS);
    }

    @DisplayName("각 타입은 반대 타입으로 매칭이 된다.")
    @ParameterizedTest
    @MethodSource("matchingProvider")
    void shouldThrowExceptionForInvalidInputs(
            String name,
            String nickName,
            String introduction,
            PrimaryRole primaryRole,
            List<PersonalityTraitConstant> traits,
            PersonalityTraitConstant primaryTrait,
            List<RelationshipPreferenceConstant> relationshipPreferenceList,
            String name2,
            String nickName2,
            String introduction2,
            PrimaryRole primaryRole2,
            List<PersonalityTraitConstant> traits2,
            PersonalityTraitConstant primaryTrait2,
            List<RelationshipPreferenceConstant> relationshipPreferenceList2) {


        registerProfile(
                name,
                nickName,
                introduction,
                primaryRole,
                traits,
                primaryTrait,
                relationshipPreferenceList);

        registerProfile(
                name2,
                nickName2,
                introduction2,
                primaryRole2,
                traits2,
                primaryTrait2,
                relationshipPreferenceList2);

        addImage(user);
        addImage(user2);

        List<UserMatching> matched1 = matchingServiceV1.match("fnel123", 0L);
        List<UserMatching> matched2 = matchingServiceV1.match("fnel1234", 0L);


        assertThat(matched1).extracting("primaryRole").containsExactly(primaryRole2);
        assertThat(matched1).extracting("nickName").containsExactly(nickName2);
        assertThat(matched1).extracting("introduction").containsExactly(introduction2);
        assertThat(matched1.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(primaryTrait2);
        assertThat(matched1.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);

        assertThat(matched2).extracting("primaryRole").containsExactly(primaryRole);
        assertThat(matched2).extracting("nickName").containsExactly(nickName);
        assertThat(matched2).extracting("introduction").containsExactly(introduction);
        assertThat(matched2.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(primaryTrait);
        assertThat(matched2.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);

    }

    public static Stream<Arguments> matchingProvider() {

        return Stream.of(
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(DEGRADER),
                        DEGRADER,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(DEGRADEE),
                        DEGRADEE,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(MASTER_MISTRESS),
                        MASTER_MISTRESS,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(SLAVE),
                        SLAVE,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(SPANKER),
                        SPANKER,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(SPANKEE),
                        SPANKEE,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(HUNTER),
                        HUNTER,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(PREY),
                        PREY,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(RIGGER),
                        RIGGER,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(ROPE_BUNNY),
                        ROPE_BUNNY,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(BRAT_TAMER),
                        BRAT_TAMER,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(BRAT),
                        BRAT,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(OWNER),
                        OWNER,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(PET),
                        PET,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(DADDY_MOMMY),
                        DADDY_MOMMY,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(LITTLE),
                        LITTLE,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(BOSS),
                        BOSS,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(SERVANT),
                        SERVANT,
                        List.of(MARRIAGE_DS)),
                Arguments.of("fnel123",
                        "알렉1",
                        "잘부탁",
                        TOP,
                        List.of(DOMINANT),
                        DOMINANT,
                        List.of(MARRIAGE_DS),
                        "fnel1234",
                        "알렉12",
                        "잘부탁",
                        BOTTOM,
                        List.of(SUBMISSIVE),
                        SUBMISSIVE,
                        List.of(MARRIAGE_DS))
        );
    }

    @DisplayName("스위치/ETC 타입은 같은 타입과 연결된다.")
    @ParameterizedTest
    @MethodSource("matchingProviderForOthers")
    void test7(
            PrimaryRole primaryRole,
            List<PersonalityTraitConstant> traits,
            PersonalityTraitConstant primaryTrait,
            PrimaryRole primaryRole2,
            String nickName
    ) {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                primaryRole,
                traits,
                primaryTrait,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                SWITCH,
                List.of(MASOCHIST, DEGRADEE),
                DEGRADEE,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12345",
                "알렉13",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        User user5 = User.builder().name("fnel1239").role(Role.ROLE_USER).build();
        userRepository.save(user5);
        registerProfile(
                "fnel1239",
                "알렉1356",
                "잘부탁",
                ETC,
                List.of(MASOCHIST, DEGRADEE),
                DEGRADEE,
                List.of(MARRIAGE_DS));


        addImage(user);
        addImage(user2);
        addImage(user3);
        addImage(user4);
        addImage(user5);

        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).extracting("primaryRole").containsExactly(primaryRole2);
        assertThat(matched).extracting("nickName").containsExactly(nickName);
        assertThat(matched).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(false, true);

    }

    public static Stream<Arguments> matchingProviderForOthers() {

        return Stream.of(
                Arguments.of(
                        SWITCH,
                        List.of(DEGRADER, SADIST),
                        SADIST,
                        SWITCH,
                        "알렉12"
                ),
                Arguments.of(
                        ETC,
                        List.of(DEGRADER, SADIST),
                        SADIST,
                        ETC,
                        "알렉1356"
                )
        );
    }

    @DisplayName("대표성향이 아닌 경우는 매칭에 포함되지 않는다.")
    @Test
    void test3() {

        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(DEGRADER, SADIST),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                DEGRADEE,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, PREY),
                PREY,
                List.of(MARRIAGE_DS));


        addImage(user);
        addImage(user2);
        addImage(user3);

        List<UserMatching> matched1 = matchingServiceV1.match("fnel123", 0L);
        List<UserMatching> matched2 = matchingServiceV1.match("fnel1234", 0L);

        assertThat(matched1).isEmpty();
        assertThat(matched2).isEmpty();
    }

    @DisplayName("이용자가 밴 당했으면 매칭에 포함되지 않는다.")
    @Test
    void test4() {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user2);
        addImage(user3);

        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        ProfileRecordResponse record =
                records.stream().filter(i -> i.nickName().equals("알렉12")).findFirst().get();
        profileMonitoringFacadeV1.rejectProfile(record.recordId(), "그냥");

        UserStats userStats = profileMonitoringFacadeV1.getUserStats(user2.getId());
        assertThat(userStats.bannedProfileCount()).isEqualTo(1L);
        assertThat(user2.getProfile().isBaned()).isTrue();

        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).hasSize(1);
        assertThat(matched).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched).extracting("nickName").containsExactly("알렉124");
        assertThat(matched).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("프로필이 없는 이용자는 매칭에 포함되지 않는다.")
    @Test
    void test5() {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user3);

        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).hasSize(1);
        assertThat(matched).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched).extracting("nickName").containsExactly("알렉124");
        assertThat(matched).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("프로필을 비공개하면 매칭에 포함되지 않는다.")
    @Test
    void test6() {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));


        addImage(user);
        addImage(user2);

        profileFacadeV1.setProfilePrivate("fnel1234");

        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).isEmpty();

        profileFacadeV1.setProfilePublic("fnel1234");
        List<UserMatching> matched2 = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched2).hasSize(1);
        assertThat(matched2).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched2).extracting("nickName").containsExactly("알렉12");
        assertThat(matched2).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched2.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched2.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("프로필을 사진이 없으면 매칭에 포함되지 않는다.")
    @Test
    void test7() {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));


        addImage(user);

        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).isEmpty();

        addImage(user2);
        List<UserMatching> matched2 = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched2).hasSize(1);
        assertThat(matched2).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched2).extracting("nickName").containsExactly("알렉12");
        assertThat(matched2).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched2.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched2.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }


    @DisplayName("프로필을 사진을 등록하지 않으면 매칭을 시작할 수 없다.")
    @Test
    void test8() {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(MASOCHIST, DEGRADEE),
                MASOCHIST,
                List.of(MARRIAGE_DS));


        assertThatThrownBy(() -> matchingServiceV1.match("fnel123", 0L))
                .isInstanceOf(CustomException.class).hasMessage("MATCHING FAIL WITHOUT IMAGE");
    }


    @DisplayName("스위치/ETC 타입은 같은 타입과 연결된다.")
    @Test
    void test9() {
        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(HUNTER),
                HUNTER,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(PREY),
                PREY,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                SWITCH,
                List.of(PREY),
                PREY,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12345",
                "알렉13",
                "잘부탁",
                ETC,
                List.of(PREY),
                PREY,
                List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user2);
        addImage(user3);
        addImage(user4);

        List<UserMatching> matched = matchingServiceV1.match("fnel123", 0L);
        assertThat(matched).hasSize(1);
        assertThat(matched).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched).extracting("nickName").containsExactly("알렉12");
        assertThat(matched).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(PREY);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);

    }

    public void registerProfile(
            String name,
            String nickName,
            String introduction,
            PrimaryRole primaryRole,
            List<PersonalityTraitConstant> traits,
            PersonalityTraitConstant primaryTrait,
            List<RelationshipPreferenceConstant> relationshipList
    ) {
        ProfileRegistration profileRegistration =
                new ProfileRegistration(
                        nickName,
                        introduction,
                        M,
                        primaryRole,
                        traits,
                        primaryTrait,
                        relationshipList,
                        LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, name);
    }

    //이미지가 없거나, 유저다 밴당하면 매칭에 x

    private void addImage(User user) {
        ProfileImage image = ProfileImage.builder()
                .fileName("aa")
                .url("aa")
                .profile(user.getProfile()).
                build();

        profileImageRepository.save(image);

        user.getProfile().addProfileImages(List.of(image));
    }

}
