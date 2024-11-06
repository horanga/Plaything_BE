package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserCredentials;
import com.plaything.api.domain.repository.entity.user.profile.RelationshipPreference;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.*;
import com.plaything.api.domain.user.model.request.ProfileRegistration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.plaything.api.domain.repository.entity.user.QUser.user;
import static com.plaything.api.domain.user.constants.Gender.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
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
                .name("dusgh123")
                .role(Role.ROLE_USER)
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
                "dusgh123","hi", M, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");

        Profile profile = profileFacadeV1.getProfileByUser("dusgh123");

        assertThat(profile.getNickName()).isEqualTo("dusgh123");
        assertThat(profile.getGender()).isEqualTo(M);
        assertThat(profile.getPrimaryRole()).isEqualTo(PrimaryRole.OTHER);
        assertThat(profile.getRelationshipPreference()).extracting("relationshipPreference").containsExactly(RelationshipPreferenceConstant.DATE_DS);
        assertThat(profile.getPersonalityTrait()).extracting("trait").containsExactly(PersonalityTraitConstant.BOSS);
        assertThat(profile.getBirthDate()).isEqualTo(now);
    }

    @DisplayName("이용자는 대표 성향을 등록할 수 있다.")
    @Test
    void test() {

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123","hi", M, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS, PersonalityTraitConstant.HUNTER), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");

        Profile profile = profileFacadeV1.getProfileByUser("dusgh123");

        assertThat(profile.getNickName()).isEqualTo("dusgh123");
        assertThat(profile.getGender()).isEqualTo(M);
        assertThat(profile.getPrimaryRole()).isEqualTo(PrimaryRole.OTHER);
        assertThat(profile.getRelationshipPreference()).extracting("relationshipPreference").containsExactly(RelationshipPreferenceConstant.DATE_DS);
        assertThat(profile.getPersonalityTrait()).extracting("trait").containsExactly(PersonalityTraitConstant.BOSS, PersonalityTraitConstant.HUNTER);
        assertThat(profile.getPersonalityTrait()).extracting("isPrimaryTrait").containsExactly(true, false);
        assertThat(profile.getBirthDate()).isEqualTo(now);
    }

    @DisplayName("닉네임을 등록하지 않으면 프로필 등록이 실패한다.")
    @Test
    void test3() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb(null, M, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS),PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("성별을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test4() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", null, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS),PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);

    }

    @DisplayName("대표 성향을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test5() {

        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", M, null, List.of(PersonalityTraitConstant.BOSS),PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);

    }

    @DisplayName("상세 성향을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test6() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", M, PrimaryRole.OTHER, Collections.emptyList(), null, List.of(RelationshipPreferenceConstant.DATE_DS), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("원하는 관계를 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test7() {
        LocalDate now = LocalDate.now();
        Profile profile = getProfileFromDb("aa", M, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS,Collections.emptyList(), now);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("생일을 등록하지 않으면 프로필을 등록하는 게 실패한다.")
    @Test
    void test8() {
        Profile profile = getProfileFromDb("aa", M, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS,List.of(RelationshipPreferenceConstant.DATE_DS), null);
        assertThatThrownBy(() -> profileRepository.save(profile))
                .isInstanceOf(Exception.class);
    }

    @DisplayName("이미 프로필을 등록한 사람은 다시 프로필을 등록할 수 없다.")
    @Test
    void test9() {
        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "dusgh123","hi", M, PrimaryRole.OTHER, List.of(PersonalityTraitConstant.BOSS),PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh123");
        assertThatThrownBy(() -> profileFacadeV1.registerProfile(profileRegistration, "dusgh123"))
                .isInstanceOf(CustomException.class).hasMessage("PROFILE ALREADY EXIST");
    }

    //이미 프로파일 있는 경우 예외

    //프로파일 공개, 비공개


    //대표성향이 선택한 성향중에 없을 때


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
                .map(i->i.checkPrimaryTrait(primaryTrait))
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