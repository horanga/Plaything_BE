package com.plaything.api.domain.matching.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.admin.service.ProfileMonitoringFacadeV1;
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
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import com.plaything.api.domain.profile.service.profileImage.ProfileImageServiceV1;
import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.chat.ChatRoomRepository;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.util.UserGenerator;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.plaything.api.domain.key.constant.KeyLogStatus.USE;
import static com.plaything.api.domain.key.constant.KeyType.POINT_KEY;
import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_BODY;
import static com.plaything.api.domain.notification.constant.NotificationMessage.MATCHING_REQUEST_TITLE;
import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;
import static com.plaything.api.domain.profile.constants.Gender.M;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.*;
import static com.plaything.api.domain.profile.constants.PrimaryRole.*;
import static com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant.MARRIAGE_DS;
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

    @Autowired
    private UserGenerator userGenerator;


    @BeforeEach
    void setUp() {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.createUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.createUser(request2);

    }

    @DisplayName("이용자는 대표성향의 반대 이용자와 매칭된다(TOP 기준)")
    @ParameterizedTest
    @MethodSource("matchingProvider")
    void test1(List<PersonalityTraitConstant> traits,
               List<PersonalityTraitConstant> traits2,
               PersonalityTraitConstant primary,
               PersonalityTraitConstant primary2) {

        userGenerator.generateWithRoles("fnel1", "123", "1", "알렉1", TOP, traits, primary);
        userGenerator.generateWithRoles("fnel2", "123", "1", "알렉2", BOTTOM, traits2, primary2);
        userGenerator.generateWithRoles("fnel3", "123", "1", "알렉3", BOTTOM, traits2, primary2);
        userGenerator.generateWithRoles("fnel4", "123", "1", "알렉4", BOTTOM, traits2, primary2);

        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉2", "a", true);
        userGenerator.addImages("알렉3", "a", true);
        userGenerator.addImages("알렉4", "a", true);

        List<UserMatching> list = matchingServiceV1.searchPartner("fnel1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0L);
        assertThat(list).hasSize(3);
        assertThat(list).extracting("primaryRole").containsExactly(BOTTOM, BOTTOM, BOTTOM);
        assertThat(list).extracting("nickName").containsExactly("알렉2", "알렉3", "알렉4");
        assertThat(list).extracting("introduction").containsExactly("hi", "hi", "hi");

        assertThat(list.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(primary2);
        assertThat(list.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);
        assertThat(list.get(1).personalityTraitList()).extracting("personalityTrait").containsExactly(primary2);
        assertThat(list.get(1).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);
        assertThat(list.get(2).personalityTraitList()).extracting("personalityTrait").containsExactly(primary2);
        assertThat(list.get(2).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);

        List<UserMatching> list2 = matchingServiceV1.searchPartner("fnel2", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0L);

        assertThat(list2).hasSize(1);
        assertThat(list2).extracting("primaryRole").containsExactly(TOP);
        assertThat(list2).extracting("nickName").containsExactly("알렉1");
        assertThat(list2).extracting("introduction").containsExactly("hi");
        assertThat(list2.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(primary);
        assertThat(list2.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true);

    }

    public static Stream<Arguments> matchingProvider() {

        return Stream.of(
                Arguments.of(
                        List.of(DEGRADER),
                        List.of(DEGRADEE),
                        DEGRADER,
                        DEGRADEE),
                Arguments.of(
                        List.of(SPANKER),
                        List.of(SPANKEE),
                        SPANKER, SPANKEE),
                Arguments.of(
                        List.of(HUNTER),
                        List.of(PREY),
                        HUNTER,
                        PREY),
                Arguments.of(
                        List.of(RIGGER),
                        List.of(ROPE_BUNNY),
                        RIGGER,
                        ROPE_BUNNY),
                Arguments.of(
                        List.of(BRAT_TAMER),
                        List.of(BRAT),
                        BRAT_TAMER,
                        BRAT),
                Arguments.of(
                        List.of(OWNER),
                        List.of(PET),
                        OWNER,
                        PET),
                Arguments.of(
                        List.of(DADDY_MOMMY),
                        List.of(LITTLE),
                        DADDY_MOMMY,
                        LITTLE),
                Arguments.of(
                        List.of(BOSS),
                        List.of(SERVANT),
                        BOSS,
                        SERVANT),
                Arguments.of(
                        List.of(DOMINANT),
                        List.of(SUBMISSIVE),
                        DOMINANT,
                        SUBMISSIVE));

    }

    @DisplayName("스위치/ETC 타입은 같은 타입과 연결된다.")
    @Test
    void test2() {
        userGenerator.generateWithRoles("fnel1", "123", "1", "알렉1", SWITCH, List.of(DEGRADER), DEGRADER);
        userGenerator.generateWithRoles("fnel2", "123", "1", "알렉2", SWITCH, List.of(SADIST), SADIST);
        userGenerator.generateWithRoles("fnel3", "123", "1", "알렉3", ETC, List.of(DEGRADER, SADIST), DEGRADER);
        userGenerator.generateWithRoles("fnel4", "123", "1", "알렉4", ETC, List.of(SADIST), SADIST);

        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉2", "a", true);
        userGenerator.addImages("알렉3", "a", true);
        userGenerator.addImages("알렉4", "a", true);

        List<UserMatching> list1 = matchingServiceV1.searchPartner("fnel1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0l);
        assertThat(list1).extracting("primaryRole").containsExactly(SWITCH);
        assertThat(list1).extracting("nickName").containsExactly("알렉2");
        assertThat(list1.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(SADIST);

        List<UserMatching> list2 = matchingServiceV1.searchPartner("fnel3", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0l);
        assertThat(list2).extracting("primaryRole").containsExactly(ETC);
        assertThat(list2).extracting("nickName").containsExactly("알렉4");
        assertThat(list2.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(SADIST);


    }

    @DisplayName("대표성향이 아닌 경우는 매칭에 포함되지 않는다.")
    @Test
    void test3() {


        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", TOP, List.of(DEGRADER, SADIST), SADIST);
        userGenerator.generateWithRoles("fnel1234", "123", "1", "알렉2", BOTTOM, List.of(MASOCHIST, DEGRADEE), DEGRADEE);
        userGenerator.generateWithRoles("fnel12344", "123", "1", "알렉3", BOTTOM, List.of(MASOCHIST, PREY), PREY);


        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉2", "a", true);
        userGenerator.addImages("알렉3", "a", true);

        List<UserMatching> matched1 = matchingServiceV1.searchPartner("fnel123", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0L);
        List<UserMatching> matched2 = matchingServiceV1.searchPartner("fnel1234", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0L);

        assertThat(matched1).isEmpty();
        assertThat(matched2).isEmpty();
    }

    @DisplayName("이용자가 밴 당했으면 매칭에 포함되지 않는다.")
    @Test
    void test4() {

        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", TOP, List.of(DEGRADER, SADIST), SADIST);
        userGenerator.generateWithRoles("fnel1234", "123", "1", "알렉2", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST);
        userGenerator.generateWithRoles("fnel12344", "123", "1", "알렉3", BOTTOM, List.of(MASOCHIST, PREY), MASOCHIST);

        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉2", "a", true);
        userGenerator.addImages("알렉3", "a", true);


        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        ProfileRecordResponse record = records.stream().filter(i -> i.nickName().equals("알렉2")).findFirst().get();
        profileMonitoringFacadeV1.rejectProfile(record.recordId(), "그냥");

        List<UserMatching> matched = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched).hasSize(1);
        assertThat(matched).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched).extracting("nickName").containsExactly("알렉3");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, PREY);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("프로필이 없는 이용자는 매칭에 포함되지 않는다.")
    @Test
    void test5() {

        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", TOP, List.of(DEGRADER, SADIST), SADIST);

        CreateUserRequest request = new CreateUserRequest("fnel11", "123", "aa");
        authServiceV1.createUser(request);
        userGenerator.generateWithRoles("fnel12344", "123", "1", "알렉124", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST);


        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉124", "a", true);


        List<UserMatching> matched = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched).hasSize(1);
        assertThat(matched).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched).extracting("nickName").containsExactly("알렉124");
        assertThat(matched.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("프로필을 비공개하면 매칭에 포함되지 않는다.")
    @Test
    void test6() {
        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", TOP, List.of(DEGRADER, SADIST), SADIST);
        userGenerator.generateWithRoles("fnel1234", "123", "1", "알렉12", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST);

        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉12", "a", true);

        profileFacadeV1.setProfilePrivate("fnel1234");

        List<UserMatching> matched = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched).isEmpty();

        profileFacadeV1.setProfilePublic("fnel1234");
        List<UserMatching> matched2 = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched2).hasSize(1);
        assertThat(matched2).extracting("primaryRole").containsExactly(BOTTOM);
        assertThat(matched2).extracting("nickName").containsExactly("알렉12");
        assertThat(matched2.get(0).personalityTraitList()).extracting("personalityTrait").containsExactly(MASOCHIST, DEGRADEE);
        assertThat(matched2.get(0).personalityTraitList()).extracting("isPrimaryTrait").containsExactly(true, false);
    }

    @DisplayName("프로필을 사진이 없으면 매칭에 포함되지 않는다.")
    @Test
    void test7() {

        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", TOP, List.of(DEGRADER, SADIST), SADIST);
        userGenerator.generateWithRoles("fnel1234", "123", "1", "알렉12", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST);


        userGenerator.addImages("알렉1", "a", true);

        List<UserMatching> matched = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched).isEmpty();

        List<UserMatching> matched2 = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched2).isEmpty();
    }


    @DisplayName("프로필 사진을 등록하지 않으면 매칭을 시작할 수 없다.")
    @Test
    void test8() {

        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", TOP, List.of(DEGRADER, SADIST), SADIST);
        userGenerator.generateWithRoles("fnel1234", "123", "1", "알렉12", BOTTOM, List.of(MASOCHIST, DEGRADEE), MASOCHIST);


        assertThatThrownBy(() -> matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS)).isInstanceOf(CustomException.class).hasMessage("등록된 프로필 사진이 없어 매칭 요청이 실패합니다");
    }


    @DisplayName("스위치/ETC 타입은 같은 타입과 연결된다.")
    @Test
    void test9() {
        userGenerator.generateWithRoles("fnel123", "123", "1", "알렉1", SWITCH, List.of(HUNTER), HUNTER);
        userGenerator.generateWithRoles("fnel1234", "123", "1", "알렉12", SWITCH, List.of(DADDY_MOMMY), DADDY_MOMMY);


        userGenerator.generateWithRoles("fnel12344", "123", "1", "알렉124", ETC, List.of(PREY), PREY);
        userGenerator.generateWithRoles("fnel12345", "123", "1", "알렉13", ETC, List.of(DADDY_MOMMY), DADDY_MOMMY);

        userGenerator.generateWithRoles("fnel6", "123", "1", "알렉4", TOP, List.of(HUNTER), HUNTER);
        userGenerator.generateWithRoles("fnel7", "123", "1", "알렉5", BOTTOM, List.of(PREY), PREY);

        userGenerator.addImages("알렉1", "a", true);
        userGenerator.addImages("알렉12", "a", true);
        userGenerator.addImages("알렉124", "a", true);
        userGenerator.addImages("알렉13", "a", true);
        userGenerator.addImages("알렉4", "a", true);
        userGenerator.addImages("알렉5", "a", true);

        List<UserMatching> matched = matchingFacadeV1.findMatchingCandidates("fnel123", 1, TimeUnit.DAYS);
        assertThat(matched).hasSize(1);
        assertThat(matched).extracting("primaryRole").containsExactly(SWITCH);
        assertThat(matched).extracting("nickName").containsExactly("알렉12");

        List<UserMatching> matched2 = matchingFacadeV1.findMatchingCandidates("fnel12344", 1, TimeUnit.DAYS);
        assertThat(matched2).hasSize(1);
        assertThat(matched2).extracting("primaryRole").containsExactly(ETC);
        assertThat(matched2).extracting("nickName").containsExactly("알렉13");

    }


    @DisplayName("매칭을 요청하면 알림이 보내진다.")
    @Test
    void test10() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        registerProfile("dusgh1234", "연호1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "adafddb");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);
        List<SavedImage> savedImages = List.of(new SavedImage("abc", true));

        profileImageServiceV1.saveImages(savedImages, "dusgh1234");

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("dusgh12345");

        em.flush();
        em.clear();
        matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, "123");

        List<NotificationResponse> notification = notificationServiceV1.getNotification("dusgh12345");

        assertThat(notification.size()).isEqualTo(1);
        assertThat(notification.get(0).title()).isEqualTo("연호1" + MATCHING_REQUEST_TITLE);
        assertThat(notification.get(0).body()).isEqualTo(MATCHING_REQUEST_BODY);
        assertThat(notification.get(0).type()).isEqualTo(MATCHING_REQUEST);
        assertThat(notification.get(0).requesterNickName()).isEqualTo("연호1");
        assertThat(notification.get(0).requesterMainPhoto()).isEqualTo("https://d25ulpahxovik9.cloudfront.net/abc");
    }


    @DisplayName("포인트 키 사용 요청의 트랜잭션 id가 동일하면 중복으로 처리된다.")
    @Test
    void test11() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        registerProfile("dusgh1234", "연호1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "122s");
        List<SavedImage> savedImages = List.of(new SavedImage("b", true));
        profileImageServiceV1.saveImages(savedImages, "dusgh1234");

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");
        MatchingRequest matchingRequest = new MatchingRequest("dusgh12345");

        em.flush();
        em.clear();
        matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, "123dsdddsd");
        assertThatThrownBy(() -> matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, "123dsdddsd")).isInstanceOf(CustomException.class)
                .hasMessage("이미 처리된 요청입니다");
    }

    @DisplayName("포인트 키를 사용하면 관련 로그들이 쌓인다.")
    @Test
    void test12() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        registerProfile("dusgh1234", "연호1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "133");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);

        List<SavedImage> savedImages = List.of(new SavedImage("b", true));

        profileImageServiceV1.saveImages(savedImages, "dusgh1234");

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS), LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("dusgh12345");

        em.flush();
        em.clear();
        matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, "4");

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
        registerProfile("dusgh1234", "연호1", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "13223");

        registerProfile("dusgh12345", "연호2", "잘부탁", TOP, List.of(SADIST, DEGRADER), SADIST, List.of(MARRIAGE_DS));

        AdRewardRequest request2 = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh12345", request2, LocalDateTime.now(), "135");

        List<SavedImage> savedImages = List.of(new SavedImage("b", true));

        profileImageServiceV1.saveImages(savedImages, "dusgh1234");

        List<SavedImage> savedImages2 = List.of(new SavedImage("b", true));

        profileImageServiceV1.saveImages(savedImages2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("dusgh12345");


        em.flush();
        em.clear();
        matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, "78894");
        MatchingRequest matchingRequest2 = new MatchingRequest("dusgh1234");
        matchingFacadeV1.acceptMatchingRequest("dusgh12345", matchingRequest2, "132");

        Matching matching = matchingRepository.findBySenderLoginIdAndReceiverLoginId("dusgh1234", "dusgh12345").get();

        assertThat(matching.isMatched()).isTrue();
        ChatRoom room = chatRoomRepository.findChatRoomByUsers("dusgh1234", "dusgh12345").get();
        assertThat(room.getLastSequence()).isEqualTo(0);
        assertThat(room.getSenderLoginId()).isEqualTo("dusgh1234");
        assertThat(room.getReceiverLoginId()).isEqualTo("dusgh12345");

    }


    // TODO CANDIDATELIST랑 테스트하기

    public void registerProfile(String name, String nickName, String introduction, PrimaryRole primaryRole, List<PersonalityTraitConstant> traits, PersonalityTraitConstant primaryTrait, List<RelationshipPreferenceConstant> relationshipList) {
        ProfileRegistration profileRegistration = new ProfileRegistration(nickName, introduction, M, primaryRole, traits, primaryTrait, relationshipList, LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, name);
    }

    //이미지가 없거나, 유저다 밴당하면 매칭에 x


}
