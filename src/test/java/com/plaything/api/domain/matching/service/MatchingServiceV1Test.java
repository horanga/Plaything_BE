package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.user.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.constants.Role;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.response.UserMatching;
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
import static com.plaything.api.domain.user.constants.PrimaryRole.BOTTOM;
import static com.plaything.api.domain.user.constants.PrimaryRole.TOP;
import static com.plaything.api.domain.user.constants.RelationshipPreferenceConstant.MARRIAGE_DS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class MatchingServiceV1Test {

    //    ONE(MASOCHIST, SADIST),
//    TWO(DEGRADER, DEGRADEE)
    @Autowired
    private MatchingServiceV1 matchingServiceV1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileImageRepository profileImageRepository;

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
                List.of(MASOCHIST, DEGRADER),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(SADIST, DEGRADEE),
                SADIST,
                List.of(MARRIAGE_DS));


        registerProfile(
                "fnel12345",
                "알렉13",
                "잘부탁",
                TOP,
                List.of(DEGRADEE),
                DEGRADEE,
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
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(SADIST, DEGRADER);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(matched.get(1).personalityTraitList()).extracting("personalityTrait").containsExactly(SADIST, DEGRADEE);
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
                List.of(DEGRADER, MASOCHIST),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(SADIST, DEGRADER),
                SADIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(SADIST, DEGRADEE),
                SADIST,
                List.of(MARRIAGE_DS));


        registerProfile(
                "fnel12345",
                "알렉13",
                "잘부탁",
                TOP,
                List.of(DEGRADEE),
                DEGRADEE,
                List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user2);
        addImage(user3);
        addImage(user4);

        List<UserMatching> matched = matchingServiceV1.match("fnel1234", 0L);
        assertThat(matched).extracting("primaryRole").containsExactly(TOP);
        assertThat(matched).extracting("nickName").containsExactly("알렉1");
        assertThat(matched).extracting("introduction").containsExactly("잘부탁");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(DEGRADER, MASOCHIST);
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

    @DisplayName("대표성향이 아닌 경우는 매칭에 포함되지 않는다.")
    @Test
    void test3() {

        registerProfile(
                "fnel123",
                "알렉1",
                "잘부탁",
                TOP,
                List.of(DEGRADER, MASOCHIST),
                MASOCHIST,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel1234",
                "알렉12",
                "잘부탁",
                BOTTOM,
                List.of(SADIST, DEGRADER),
                DEGRADER,
                List.of(MARRIAGE_DS));

        registerProfile(
                "fnel12344",
                "알렉124",
                "잘부탁",
                BOTTOM,
                List.of(SADIST, MASOCHIST),
                MASOCHIST,
                List.of(MARRIAGE_DS));


        addImage(user);
        addImage(user2);
        addImage(user3);

        List<UserMatching> matched1 = matchingServiceV1.match("fnel123", 0L);
        List<UserMatching> matched2 = matchingServiceV1.match("fnel1234", 0L);

        assertThat(matched1).isEmpty();
        assertThat(matched2).isEmpty();
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
