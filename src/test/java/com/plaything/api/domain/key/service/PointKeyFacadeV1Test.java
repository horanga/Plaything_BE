package com.plaything.api.domain.key.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.model.response.AdViewLogResponse;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.key.model.response.PointKeyLog;
import com.plaything.api.domain.key.model.response.PointKeyUsageLog;
import com.plaything.api.domain.notification.model.response.NotificationResponse;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.pay.UserRewardActivityRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.ProfileImageServiceV1;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.plaything.api.domain.key.constant.KeyLogStatus.EARN;
import static com.plaything.api.domain.key.constant.KeyLogStatus.USE;
import static com.plaything.api.domain.key.constant.KeySource.ADVERTISEMENT_REWARD;
import static com.plaything.api.domain.key.constant.KeySource.LOGIN_REWARD;
import static com.plaything.api.domain.key.constant.KeyType.POINT_KEY;
import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;
import static com.plaything.api.domain.notification.model.response.NotificationMessage.MATCHING_REQUEST_BODY;
import static com.plaything.api.domain.notification.model.response.NotificationMessage.MATCHING_REQUEST_TITLE;
import static com.plaything.api.domain.user.constants.Gender.M;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.HUNTER;
import static com.plaything.api.domain.user.constants.PrimaryRole.TOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.within;
@Transactional
@SpringBootTest
class PointKeyFacadeV1Test {

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private UserRewardActivityRepository userRewardActivityRepository;

    @Autowired
    private AdLogServiceV1 adLogServiceV1;

    @Autowired
    private PointKeyLogServiceV1 pointKeyLogServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationServiceV1 notificationServiceV1;

    @Autowired
    private ProfileImageServiceV1 profileImageServiceV1;

    @Autowired
    private EntityManager em;

    @Autowired
    protected RedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {

        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }


        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);
    }

    @DisplayName("매일 첫 로그인 시 포인트 키를 제공한다.")
    @Test
    void test1() {
        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        LoginResponse login = authServiceV1.login(request, LocalDate.now(), "1");

        AvailablePointKey availablePointKey = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey.availablePointKey()).isEqualTo(1L);
        assertThat(login.dailyRewardProvided()).isTrue();

        LoginRequest request2 = new LoginRequest("dusgh1234", "1234");
        LoginResponse login2 = authServiceV1.login(request2, LocalDate.now(), "2");

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(1L);
        assertThat(login2.dailyRewardProvided()).isFalse();

        LoginRequest request3 = new LoginRequest("dusgh1234", "1234");
        LoginResponse login3 = authServiceV1.login(request3, LocalDate.now().plusDays(1L), "3");

        AvailablePointKey availablePointKey3 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey3.availablePointKey()).isEqualTo(2L);
        assertThat(login3.dailyRewardProvided()).isTrue();

    }

    @DisplayName("로그인 보상을 받으면 로그인 이력이 남는다.")
    @Test
    void test() {

        LocalDate now = LocalDate.now().minusDays(1);
        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        authServiceV1.login(request, now, "1");
        UserRewardActivity activity = userRewardActivityRepository.findByUser_loginId("dusgh1234").get();
        assertThat(activity.getLastLoginTime()).isEqualTo(now);

        authServiceV1.login(request, now.plusDays(2), "2");
        UserRewardActivity activity2 = userRewardActivityRepository.findByUser_loginId("dusgh1234").get();
        assertThat(activity2.getLastLoginTime()).isEqualTo(now.plusDays(2));
    }

    @DisplayName("로그인 보상을 받으면 로그로 남는다.")
    @Test
    void test5() {

        LocalDate now = LocalDate.now();
        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        authServiceV1.login(request, now, "1");

        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(1L);

        List<PointKeyLog> pointKeyLogs = pointKeyLogServiceV1.getPointKeyLog("dusgh1234");
        assertThat(pointKeyLogs.size()).isEqualTo(1);
        assertThat(pointKeyLogs).extracting("keyType").containsExactly(POINT_KEY);
        assertThat(pointKeyLogs).extracting("keyLogStatus").containsExactly(EARN);
        assertThat(pointKeyLogs).extracting("keySource").containsExactly(LOGIN_REWARD);
        assertThat(pointKeyLogs).extracting("userLoginId").containsExactly("dusgh1234");
        assertThat(pointKeyLogs).extracting("keyExpirationDate").allSatisfy(date ->
                assertThat((LocalDateTime) date)
                        .isCloseTo(
                                LocalDateTime.now().plusMonths(6),
                                within(1, ChronoUnit.MINUTES)
                        ));

    }

    @DisplayName("광고를 보면 포인트 키를 제공한다.")
    @Test
    void test2() {

        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");
        LoginRequest loginRequest = new LoginRequest("dusgh1234", "1234");
        LoginResponse login = authServiceV1.login(loginRequest, LocalDate.now(), "2");

        AvailablePointKey availablePointKey = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey.availablePointKey()).isEqualTo(3L);
        assertThat(login.dailyRewardProvided()).isTrue();
    }

    @DisplayName("정해진 시간을 지나면 광고를 통해 포인트 키를 받을 수 있다.")
    @Test
    void test4() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");
        AdRewardRequest request2 = new AdRewardRequest("광고1", 2);
        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request2, LocalDateTime.now(), "2"))
                .isInstanceOf(CustomException.class)
                .hasMessage("AD_VIEW_TIME_NOT_EXPIRED");


        AdRewardRequest request3 = new AdRewardRequest("광고1", 2);
        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request3, LocalDateTime.now().plusHours(3).plusMinutes(59), "3"))
                .isInstanceOf(CustomException.class)
                .hasMessage("AD_VIEW_TIME_NOT_EXPIRED");

        AdRewardRequest request4 = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request4, LocalDateTime.now().plusHours(4).plusMinutes(1), "4");

        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(4L);
    }

    @DisplayName("광고를 보고 포인트 키를 받으면 관련 로그들이 쌓인다.")
    @Test
    void test6() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);

        List<AdViewLogResponse> adViewLog = adLogServiceV1.getAdViewLog("dusgh1234");
        assertThat(adViewLog.size()).isEqualTo(1);
        assertThat(adViewLog.get(0).adType()).isEqualTo("광고1");
        assertThat(adViewLog.get(0).viewDuration()).isEqualTo(2);
        assertThat(adViewLog.get(0).userResponse().loginId()).isEqualTo("dusgh1234");

        List<PointKeyLog> pointKeyLogs = pointKeyLogServiceV1.getPointKeyLog("dusgh1234");
        assertThat(pointKeyLogs.size()).isEqualTo(2);
        assertThat(pointKeyLogs).extracting("keyType").containsExactly(POINT_KEY, POINT_KEY);
        assertThat(pointKeyLogs).extracting("keyLogStatus").containsExactly(EARN, EARN);
        assertThat(pointKeyLogs).extracting("keySource").containsExactly(ADVERTISEMENT_REWARD, ADVERTISEMENT_REWARD);
        assertThat(pointKeyLogs).extracting("userLoginId").containsExactly("dusgh1234", "dusgh1234");
        assertThat(pointKeyLogs).extracting("keyExpirationDate").allSatisfy(date ->
                assertThat((LocalDateTime) date)
                        .isCloseTo(
                                LocalDateTime.now().plusMonths(6),
                                within(1, ChronoUnit.MINUTES)
                        )
        );
    }

    @DisplayName("포인트 키를 사용하면 관련 로그들이 쌓인다.")
    @Test
    void test7() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        pointKeyFacadeV1.usePointKeyForMatching("dusgh1234", matchingRequest, "2");

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

    @DisplayName("매칭을 요청하면 알림이 보내진다.")
    @Test
    void test8() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");
        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        pointKeyFacadeV1.usePointKeyForMatching("dusgh1234", matchingRequest, "2");

        List<NotificationResponse> notification = notificationServiceV1.getNotification("dusgh12345");

        assertThat(notification.size()).isEqualTo(1);
        assertThat(notification.get(0).title()).isEqualTo("연호1" + MATCHING_REQUEST_TITLE);
        assertThat(notification.get(0).body()).isEqualTo(MATCHING_REQUEST_BODY);
        assertThat(notification.get(0).type()).isEqualTo(MATCHING_REQUEST);
        assertThat(notification.get(0).requesterNickName()).isEqualTo("연호1");
        assertThat(notification.get(0).requesterMainPhoto()).isEqualTo("a");
    }

    @DisplayName("광고는 시간 간격에 맞춰서 봐야 포인트를 받을 수 있다.")
    @Test
    void test3() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");

        AdRewardRequest request2 = new AdRewardRequest("광고1", 2);
        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request2, LocalDateTime.now(), "2"))
                .isInstanceOf(CustomException.class).hasMessage("AD_VIEW_TIME_NOT_EXPIRED");

        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(2L);
    }

    @DisplayName("트랜잭션 id가 동일하면 로그인 요청이 중복으로 처리된다")
    @Test
    void test9() {
        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        LoginResponse login = authServiceV1.login(request, LocalDate.now(), "1");

        AvailablePointKey availablePointKey = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey.availablePointKey()).isEqualTo(1L);
        assertThat(login.dailyRewardProvided()).isTrue();

        LoginRequest request2 = new LoginRequest("dusgh1234", "1234");
        assertThatThrownBy(() -> authServiceV1.login(request2, LocalDate.now(), "1"))
                .isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");

    }

    @DisplayName("광고 시청에 대한 트랜잭션 id가 동일하면 중복으로 처리된다.")
    @Test
    void test10() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");

        AdRewardRequest request2 = new AdRewardRequest("광고1", 2);
        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request2, LocalDateTime.now(), "1"))
                .isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");
    }

    @DisplayName("포인트 키 사용 요청의 트랜잭션 id가 동일하면 중복으로 처리된다.")
    @Test
    void test11() {
        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));
        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");
        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        pointKeyFacadeV1.usePointKeyForMatching("dusgh1234", matchingRequest, "2");
        assertThatThrownBy(() -> pointKeyFacadeV1.usePointKeyForMatching("dusgh1234", matchingRequest, "2"))
                .isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");


    }
}