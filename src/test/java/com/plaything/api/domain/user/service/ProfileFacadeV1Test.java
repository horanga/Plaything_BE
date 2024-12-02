package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserCredentials;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.RelationshipPreference;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.*;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.response.ProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.plaything.api.domain.user.constants.Gender.*;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.*;
import static com.plaything.api.domain.user.constants.PrimaryRole.*;
import static com.plaything.api.domain.user.constants.ProfileStatus.NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@Transactional
@SpringBootTest
class ProfileFacadeV1Test {

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {


        User user = User.builder()
                .loginId("dusgh123")
                .role(Role.ROLE_USER)
                .fcmToken("1")
                .build();
        UserCredentials password = UserCredentials.builder()
                .hashedPassword("1234")
                .build();

        user.setCredentials(password);

        userRepository.save(user);

    }

    @DisplayName("이용자가 프로필을 등록한다.")
    @Test
    void test1() {

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");

        Profile profile = profileFacadeV1.getProfileByUserLoginId("dusgh123");

        assertThat(profile.getNickName()).isEqualTo("dusgh123");
        assertThat(profile.getGender()).isEqualTo(M);
        assertThat(profile.getPrimaryRole()).isEqualTo(TOP);
        assertThat(profile.getRelationshipPreference()).extracting("relationshipPreference").containsExactly(RelationshipPreferenceConstant.DATE_DS);
        assertThat(profile.getPersonalityTrait()).extracting("trait").containsExactly(PersonalityTraitConstant.BOSS);
        assertThat(profile.getBirthDate()).isEqualTo(now);
    }

    @DisplayName("이용자는 대표 성향을 등록할 수 있다.")
    @Test
    void test2() {

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, TOP, List.of(PersonalityTraitConstant.BOSS, HUNTER), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");

        Profile profile = profileFacadeV1.getProfileByUserLoginId("dusgh123");

        assertThat(profile.getNickName()).isEqualTo("dusgh123");
        assertThat(profile.getGender()).isEqualTo(M);
        assertThat(profile.getPrimaryRole()).isEqualTo(TOP);
        assertThat(profile.getRelationshipPreference()).extracting("relationshipPreference").containsExactly(RelationshipPreferenceConstant.DATE_DS);
        assertThat(profile.getPersonalityTrait()).extracting("trait").containsExactly(PersonalityTraitConstant.BOSS, HUNTER);
        assertThat(profile.getPersonalityTrait()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(profile.getBirthDate()).isEqualTo(now);
    }

    @DisplayName("닉네임을 등록하지 않으면 프로필 등록이 실패한다.")
    @Test
    void test3() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb(null, M, TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("성별을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test4() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", null, TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);

    }

    @DisplayName("대표 성향을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test5() {

        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", M, null, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);

    }

    @DisplayName("상세 성향을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test6() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", M, TOP, Collections.emptyList(), null, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("원하는 관계를 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test7() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", M, TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, Collections.emptyList(), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("생일을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test8() {
        Profile profile = getProfileFromDb("aa", M, TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), null);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("이미 프로필을 등록한 사람은 다시 프로필을 등록할 수 없다.")
    @Test
    void test9() {
        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");
        assertThatThrownBy(() -> profileFacadeV1.registerProfile(profileRegistration, "dusgh123"))
                .isInstanceOf(CustomException.class).hasMessage("PROFILE ALREADY EXIST");
    }

    @DisplayName("프로필 정보를 조회할 수 있다.")
    @ParameterizedTest
    @MethodSource("profileProvider")
    void test10(
            Gender gender,
            PrimaryRole primaryRole,
            List<PersonalityTraitConstant> traits,
            PersonalityTraitConstant primaryTrait,
            PersonalityTraitConstant trait1,
            PersonalityTraitConstant trait2,
            Gender gender2,
            String role
    ) {
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", gender, primaryRole, traits, primaryTrait, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");
        ProfileResponse profile = profileFacadeV1.getProfileByLoginId("dusgh123");

        assertThat(profile.age()).isEqualTo(29);
        assertThat(profile.introduction()).isEqualTo("hi");
        assertThat(profile.profileStatus()).isEqualTo(NEW);
        assertThat(profile.primaryRole()).isEqualTo(role);
        assertThat(profile.isBaned()).isFalse();
        assertThat(profile.gender()).isEqualTo(gender2);
        assertThat(profile.isPrivate()).isFalse();
        assertThat(profile.personalityTrait()).extracting("personalityTrait").containsExactly(trait1, trait2);
        assertThat(profile.personalityTrait()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(profile.relationshipPreference()).extracting("relationshipPreference").containsExactly(RelationshipPreferenceConstant.DATE_DS);
    }

    public static Stream<Arguments> profileProvider() {

        return Stream.of(
                Arguments.of(
                        M,
                        TOP,
                        List.of(PersonalityTraitConstant.BOSS, HUNTER),
                        PersonalityTraitConstant.BOSS,
                        PersonalityTraitConstant.BOSS,
                        HUNTER,
                        M,
                        "MT"
                ), Arguments.of(
                        F,
                        TOP,
                        List.of(PersonalityTraitConstant.BOSS, HUNTER),
                        PersonalityTraitConstant.BOSS,
                        PersonalityTraitConstant.BOSS,
                        HUNTER,
                        F,
                        "FT"
                ),
                Arguments.of(
                        OTHER,
                        TOP,
                        List.of(PersonalityTraitConstant.BOSS, HUNTER),
                        PersonalityTraitConstant.BOSS,
                        PersonalityTraitConstant.BOSS,
                        HUNTER,
                        OTHER,
                        "TOP"
                ),
                Arguments.of(
                        M,
                        BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        M,
                        "MS"
                ),
                Arguments.of(
                        F,
                        BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        F,
                        "FS"
                ),
                Arguments.of(
                        OTHER,
                        BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        OTHER,
                        "BTM"
                ),
                Arguments.of(
                        M,
                        SWITCH,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        M,
                        "MSW"
                ),
                Arguments.of(
                        F,
                        SWITCH,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        F,
                        "FSW"
                ),
                Arguments.of(
                        OTHER,
                        SWITCH,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        OTHER,
                        "SW"
                ),
                Arguments.of(
                        M,
                        ETC,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        M,
                        "M_ETC"
                ),
                Arguments.of(
                        F,
                        ETC,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        F,
                        "F_ETC"
                ),
                Arguments.of(
                        OTHER,
                        ETC,
                        List.of(PersonalityTraitConstant.SERVANT, PersonalityTraitConstant.DEGRADEE),
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.SERVANT,
                        PersonalityTraitConstant.DEGRADEE,
                        OTHER,
                        "ETC"
                )

        );
    }

    @DisplayName("대표 그룹과 맞지 않는 세부 성향을 설정할 수 없다.")
    @ParameterizedTest
    @MethodSource("traitProvider")
    void test11(
            PrimaryRole primaryRole,
            List<PersonalityTraitConstant> traits,
            PersonalityTraitConstant primaryTrait
    ) {
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, primaryRole, traits, primaryTrait, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.of(1995, 3, 30));

        assertThatThrownBy(() -> profileFacadeV1.registerProfile(profileRegistration, "dusgh123"))
                .isInstanceOf(CustomException.class).hasMessage("ROLE MISMATCH");
    }

    public static Stream<Arguments> traitProvider() {
        return Stream.of(
                Arguments.of(
                        BOTTOM,
                        List.of(DADDY_MOMMY),
                        DADDY_MOMMY
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(HUNTER),
                        HUNTER
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(MASTER_MISTRESS),
                        MASTER_MISTRESS
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(OWNER),
                        OWNER
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(RIGGER),
                        RIGGER
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(SPANKER),
                        SPANKER
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(BRAT_TAMER),
                        BRAT_TAMER
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(BOSS),
                        BOSS
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(SADIST),
                        SADIST
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(DEGRADER),
                        DEGRADER
                ),
                Arguments.of(
                        BOTTOM,
                        List.of(DOMINANT),
                        DEGRADER
                ),
                Arguments.of(
                        TOP,
                        List.of(PREY),
                        PREY
                ),
                Arguments.of(
                        TOP,
                        List.of(LITTLE),
                        LITTLE
                ),
                Arguments.of(
                        TOP,
                        List.of(SLAVE),
                        SLAVE
                ),
                Arguments.of(
                        TOP,
                        List.of(PET),
                        PET
                ),
                Arguments.of(
                        TOP,
                        List.of(ROPE_BUNNY),
                        ROPE_BUNNY
                ),
                Arguments.of(
                        TOP,
                        List.of(SPANKEE),
                        SPANKEE
                ),
                Arguments.of(
                        TOP,
                        List.of(BRAT),
                        BRAT
                ),
                Arguments.of(
                        TOP,
                        List.of(SERVANT),
                        SERVANT
                ),
                Arguments.of(
                        TOP,
                        List.of(MASOCHIST),
                        MASOCHIST
                ),
                Arguments.of(
                        TOP,
                        List.of(DEGRADEE),
                        DEGRADEE
                ),
                Arguments.of(
                        TOP,
                        List.of(SUBMISSIVE),
                        SUBMISSIVE
                )

        );
    }

    @DisplayName("이미 프로필이 있으면 프로필 신규 등록을 할 수 없다.")
    @Test
    void test12() {
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");

        assertThatThrownBy(() -> profileFacadeV1.registerProfile(profileRegistration, "dusgh123"))
                .isInstanceOf(CustomException.class).hasMessage("PROFILE ALREADY EXIST");
    }

    @DisplayName("대표 성향이 세부성향에 있지 않으면 프로필 등록에 실패한다.")
    @Test
    void test13() {
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, TOP, List.of(HUNTER), SADIST, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.of(1995, 3, 30));

        assertThatThrownBy(() -> profileFacadeV1.registerProfile(profileRegistration, "dusgh123"))
                .isInstanceOf(CustomException.class).hasMessage("TRAITS NOT INCLUDE PRIMARY TRAIT");

    }

    @DisplayName("프로필을 비공개할 수 있다.")
    @Test
    void test14() {
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123", "hi", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.DATE_DS), LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");
        profileFacadeV1.setProfilePrivate("dusgh123");

        ProfileResponse profile = profileFacadeV1.getProfileByLoginId("dusgh123");
        assertThat(profile.isPrivate()).isTrue();

    }

    private Profile getProfileFromDb(
            String nickname,
            Gender gender,
            PrimaryRole primaryRole,
            List<PersonalityTraitConstant> personalityTraitConstant,
            PersonalityTraitConstant primaryTrait,
            List<RelationshipPreferenceConstant> relationshipPreferenceConstant,
            LocalDate localDate) {

        List<PersonalityTrait> list1 =
                personalityTraitConstant.stream()
                        .map(i -> PersonalityTrait.builder().trait(i).build())
                        .map(i -> i.checkPrimaryTrait(primaryTrait))
                        .toList();
        List<RelationshipPreference> list2 = relationshipPreferenceConstant.stream()
                .map(i -> RelationshipPreference.builder().relationshipPreference(i).build())
                .toList();

        Profile profile = Profile.builder()
                .nickName(nickname)
                .introduction("hi")
                .gender(gender)
                .primaryRole(primaryRole)
                .birthDate(localDate)
                .build();
        profile.addPersonalityTrait(list1);
        profile.addRelationshipPreference(list2);

        return profile;
    }

}