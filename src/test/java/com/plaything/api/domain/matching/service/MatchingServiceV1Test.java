package com.plaything.api.domain.matching.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.sevice.ProfileMonitoringFacadeV1;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.key.model.response.PointKeyUsageLog;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.key.service.PointKeyLogServiceV1;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.notification.model.response.NotificationResponse;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.repository.repo.user.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.constants.Role;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.model.response.UserStats;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.ProfileImageServiceV1;
import jakarta.persistence.EntityManager;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.plaything.api.domain.key.constant.KeyLogStatus.USE;
import static com.plaything.api.domain.key.constant.KeyType.POINT_KEY;
import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_BODY;
import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_TITLE;
import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;
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

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    private ProfileImageServiceV1 profileImageServiceV1;

    @Autowired
    private EntityManager em;

    @Autowired
    private NotificationServiceV1 notificationServiceV1;

    @Autowired
    private PointKeyLogServiceV1 pointKeyLogServiceV1;

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    User user;

    User user2;

    User user3;

    User user4;

    @BeforeEach
    void setUp() {


        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);

        user = User.builder().loginId("fnel123").role(Role.ROLE_USER).fcmToken("1").build();
        userRepository.save(user);
        user2 = User.builder().loginId("fnel1234").role(Role.ROLE_USER).fcmToken("1").build();
        userRepository.save(user2);
        user3 = User.builder().loginId("fnel12344").role(Role.ROLE_USER).fcmToken("1").build();
        userRepository.save(user3);
        user4 = User.builder().loginId("fnel12345").role(Role.ROLE_USER).fcmToken("1").build();
        userRepository.save(user4);
    }

    @DisplayName("이용자는 대표성향의 반대 이용자와 매칭된다.(마조히스트))")
    @Test
    void test1() {

        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));


        registerProfile("fnel12345", "알렉13", "잘부탁", TOP, List.of(DEGRADER), DEGRADER, List.of(MARRIAGE_DS));

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

        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(DEGRADER, SADIST), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));


        registerProfile("fnel12345", "알렉13", "잘부탁", TOP, List.of(DEGRADER), DEGRADER, List.of(MARRIAGE_DS));

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
    void shouldThrowExceptionForInvalidInputs(String name, String nickName, String introduction, PrimaryRole primaryRole, List<PersonalityTraitConstant> traits, PersonalityTraitConstant primaryTrait, List<RelationshipPreferenceConstant> relationshipPreferenceList, String name2, String nickName2, String introduction2, PrimaryRole primaryRole2, List<PersonalityTraitConstant> traits2, PersonalityTraitConstant primaryTrait2, List<RelationshipPreferenceConstant> relationshipPreferenceList2) {


        registerProfile(name, nickName, introduction, primaryRole, traits, primaryTrait, relationshipPreferenceList);

        registerProfile(name2, nickName2, introduction2, primaryRole2, traits2, primaryTrait2, relationshipPreferenceList2);

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

        return Stream.of(Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(DEGRADER), DEGRADER, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(DEGRADEE), DEGRADEE, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(MASTER_MISTRESS), MASTER_MISTRESS, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(SLAVE), SLAVE, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(SPANKER), SPANKER, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(SPANKEE), SPANKEE, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(HUNTER), HUNTER, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(PREY), PREY, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(RIGGER), RIGGER, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(ROPE_BUNNY), ROPE_BUNNY, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(BRAT_TAMER), BRAT_TAMER, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(BRAT), BRAT, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(OWNER), OWNER, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(PET), PET, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(DADDY_MOMMY), DADDY_MOMMY, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(LITTLE), LITTLE, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(BOSS), BOSS, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(SERVANT), SERVANT, List.of(MARRIAGE_DS)), Arguments.of("fnel123", "알렉1", "잘부탁", TOP, List.of(DOMINANT), DOMINANT, List.of(MARRIAGE_DS), "fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(SUBMISSIVE), SUBMISSIVE, List.of(MARRIAGE_DS)));
    }

    @DisplayName("스위치/ETC 타입은 같은 타입과 연결된다.")
    @ParameterizedTest
    @MethodSource("matchingProviderForOthers")
    void test7(PrimaryRole primaryRole, List<PersonalityTraitConstant> traits, PersonalityTraitConstant primaryTrait, PrimaryRole primaryRole2, String nickName) {
        registerProfile("fnel123", "알렉1", "잘부탁", primaryRole, traits, primaryTrait, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", SWITCH, List.of(MASOCHIST, DEGRADEE), DEGRADEE, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

        registerProfile("fnel12345", "알렉13", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        User user5 = User.builder().loginId("fnel1239").role(Role.ROLE_USER).fcmToken("1").build();
        userRepository.save(user5);
        registerProfile("fnel1239", "알렉1356", "잘부탁", ETC, List.of(MASOCHIST, DEGRADEE), DEGRADEE, List.of(MARRIAGE_DS));


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

        return Stream.of(Arguments.of(SWITCH, List.of(DEGRADER, SADIST), SADIST, SWITCH, "알렉12"), Arguments.of(ETC, List.of(DEGRADER, SADIST), SADIST, ETC, "알렉1356"));
    }

    @DisplayName("대표성향이 아닌 경우는 매칭에 포함되지 않는다.")
    @Test
    void test3() {

        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(DEGRADER, SADIST), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), DEGRADEE, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", BOTTOM, List.of(MASOCHIST, PREY), PREY, List.of(MARRIAGE_DS));


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
        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

        addImage(user);
        addImage(user2);
        addImage(user3);

        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        ProfileRecordResponse record = records.stream().filter(i -> i.nickName().equals("알렉12")).findFirst().get();
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
        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));

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
        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));


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
        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));


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
        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST, List.of(MARRIAGE_DS));


        assertThatThrownBy(() -> matchingServiceV1.match("fnel123", 0L)).isInstanceOf(CustomException.class).hasMessage("등록된 프로필 사진이 없어 매칭 요청이 실패합니다");
    }


    @DisplayName("스위치/ETC 타입은 같은 타입과 연결된다.")
    @Test
    void test9() {
        registerProfile("fnel123", "알렉1", "잘부탁", TOP, List.of(HUNTER), HUNTER, List.of(MARRIAGE_DS));

        registerProfile("fnel1234", "알렉12", "잘부탁", BOTTOM, List.of(PREY), PREY, List.of(MARRIAGE_DS));

        registerProfile("fnel12344", "알렉124", "잘부탁", SWITCH, List.of(PREY), PREY, List.of(MARRIAGE_DS));

        registerProfile("fnel12345", "알렉13", "잘부탁", ETC, List.of(PREY), PREY, List.of(MARRIAGE_DS));

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


    @DisplayName("매칭을 요청하면 알림이 보내진다.")
    @Test
    void test10() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "123");

        List<NotificationResponse> notification = notificationServiceV1.getNotification("dusgh12345");

        assertThat(notification.size()).isEqualTo(1);
        assertThat(notification.get(0).title()).isEqualTo("연호1" + MATCHING_REQUEST_TITLE);
        assertThat(notification.get(0).body()).isEqualTo(MATCHING_REQUEST_BODY);
        assertThat(notification.get(0).type()).isEqualTo(MATCHING_REQUEST);
        assertThat(notification.get(0).requesterNickName()).isEqualTo("연호1");
        assertThat(notification.get(0).requesterMainPhoto()).isEqualTo("a");
    }


    @DisplayName("포인트 키 사용 요청의 트랜잭션 id가 동일하면 중복으로 처리된다.")
    @Test
    void test11() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "122s");

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));
        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");
        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "123dsd");
        assertThatThrownBy(() -> matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "123")).isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");
    }

    @DisplayName("포인트 키를 사용하면 관련 로그들이 쌓인다.")
    @Test
    void test12() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "133");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "4");

        List<PointKeyUsageLog> pointKeyLogs = pointKeyLogServiceV1.getPointKeyUsageLog("dusgh1234");
        assertThat(pointKeyLogs.size()).isEqualTo(1);
        assertThat(pointKeyLogs).extracting("keyType").containsExactly(POINT_KEY);
        assertThat(pointKeyLogs).extracting("keyLogStatus").containsExactly(USE);
        assertThat(pointKeyLogs).extracting("userLoginId").containsExactly("dusgh1234");

        User user1 = userRepository.findByLoginId("dusgh1234").get();
        User user2 = userRepository.findByLoginId("dusgh12345").get();
        assertThat(pointKeyLogs.get(0).keyUsageLog().senderId()).isEqualTo(user1.getId());
        assertThat(pointKeyLogs.get(0).keyUsageLog().receiverId()).isEqualTo(user2.getId());
    }

    @DisplayName("매칭을 성사되면 채팅방이 생성된다")
    @Test
    void test13() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "13223");

        AdRewardRequest request2 = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh12345", request2, LocalDateTime.now(), "135");


        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호2", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        User user2 = userRepository.findByLoginId("dusgh1234").get();
        Profile profile2 = user.getProfile();
        List<SavedImage> savedImages2 = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages2, profile2, 0L);

        MatchingRequest matchingRequest = new MatchingRequest("연호2");


        em.flush();
        em.clear();
        matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "78894");
        MatchingRequest matchingRequest2 = new MatchingRequest("연호1");
        matchingFacadeV1.acceptMatching("dusgh12345", matchingRequest2, "132");

        Matching matching = matchingRepository.findBySenderNicknameAndReceiverNickname("연호1", "연호2").get();

        assertThat(matching.isMatched()).isTrue();
        ChatRoom room = chatRoomRepository.findChatRoomByUsers("연호1", "연호2").get();
        assertThat(room.getLastSequence()).isEqualTo(0);
        assertThat(room.getSenderNickname()).isEqualTo("연호1");
        assertThat(room.getReceiverNickname()).isEqualTo("연호2");
        
    }


    public void registerProfile(String name, String nickName, String introduction, PrimaryRole primaryRole, List<PersonalityTraitConstant> traits, PersonalityTraitConstant primaryTrait, List<RelationshipPreferenceConstant> relationshipList) {
        ProfileRegistration profileRegistration = new ProfileRegistration(nickName, introduction, M, primaryRole, traits, primaryTrait, relationshipList, LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, name);
    }

    //이미지가 없거나, 유저다 밴당하면 매칭에 x

    private void addImage(User user) {
        ProfileImage image = ProfileImage.builder().fileName("aa").url("aa").profile(user.getProfile()).build();

        profileImageRepository.save(image);

        user.getProfile().addProfileImages(List.of(image));
    }


}
