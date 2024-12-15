package com.plaything.api.domain.key.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.matching.service.MatchingFacadeV1;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import com.plaything.api.domain.repository.entity.notification.Notification;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.log.KeyLogRepository;
import com.plaything.api.domain.repository.repo.monitor.ProfileRecordRepository;
import com.plaything.api.domain.repository.repo.notification.NotificationRepository;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.pay.UserRewardActivityRepository;
import com.plaything.api.domain.repository.repo.user.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.plaything.api.domain.user.constants.Gender.M;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Transactional
@SpringBootTest
public class PointKeyUsageRollbackTest {

    @Autowired
    private KeyLogRepository keyLogRepository;

    @Autowired
    private PointKeyRepository pointKeyRepository;

    @Autowired
    private UserRewardActivityRepository userRewardActivityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private NotificationServiceV1 notificationServiceV1;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileRecordRepository profileRecordRepository;

    @Autowired
    private ProfileImageRepository profileImageRepository;

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

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration(
                "알렉1", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");

        Profile profile1 = profileRepository.findByNickName("알렉1");
        ProfileImage image1 = ProfileImage.builder().profile(profile1).url("abc").isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));

        ProfileRegistration profileRegistration2 = new ProfileRegistration(
                "알렉2", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        Profile profile2 = profileRepository.findByNickName("알렉2");

        ProfileImage image2 = ProfileImage.builder().profile(profile2).url("abcd").isMainPhoto(true).build();
        profile2.addProfileImages(List.of(image2));
    }

    @AfterEach
    void cleanup() {
        // 1. profile_record 먼저 삭제
        // (user를 참조하고 있는 자식 테이블이라 먼저 삭제 필요)
        profileRecordRepository.deleteAll();

        // 2. profile 관련 테이블들 삭제
        profileImageRepository.deleteAll();  // profile의 자식 테이블
        profileRepository.deleteAll();       // profile 메인 테이블

        // 3. key 관련 테이블들 삭제
        keyLogRepository.deleteAll();
        pointKeyRepository.deleteAll();
        userRewardActivityRepository.deleteAll();

        // 4. 마지막으로 user 삭제
        userRepository.deleteAll();

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }


    @DisplayName("포인트 키 사용중 체크 예외가 발생하면 키 사용 이력이 롤백된다")
    @Test
    void test1() throws IOException {

        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        authServiceV1.login(request, LocalDate.now(), "1");

        // 첫 번째 트랜잭션 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();
        // 새로운 트랜잭션 시작
        TestTransaction.start();

        doThrow(new IOException())
                .when(notificationServiceV1)
                .saveNotification(any(), any(), any());

        User user = userRepository.findByLoginId("dusgh12345").get();
        MatchingRequest matchingRequest = new MatchingRequest(user.getNickname());
        assertThatThrownBy(() -> matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "123"))
                .isInstanceOf(CustomException.class);

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(1L);
        List<KeyLog> list = keyLogRepository.findByUser_LoginIdAndKeyLogStatus("dusgh1234", KeyLogStatus.USE);
        assertThat(list.isEmpty()).isTrue();
        List<Notification> notifications = notificationRepository.findByReceiver_LoginId("dusgh12345");
        assertThat(notifications.isEmpty()).isTrue();
    }

    @DisplayName("포인트 키 사용중 언체크 예외가 발생하면 키 사용 이력이 롤백된다")
    @Test
    void test2() throws IOException {

        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        authServiceV1.login(request, LocalDate.now(), "1");

        // 첫 번째 트랜잭션 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();
        // 새로운 트랜잭션 시작
        TestTransaction.start();

        doThrow(new RuntimeException())
                .when(notificationServiceV1)
                .saveNotification(any(), any(), any());

        User user = userRepository.findByLoginId("dusgh12345").get();
        MatchingRequest matchingRequest = new MatchingRequest(user.getNickname());
        assertThatThrownBy(() -> matchingFacadeV1.createMatching("dusgh1234", matchingRequest, "123"))
                .isInstanceOf(RuntimeException.class);

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(1L);
        List<KeyLog> list = keyLogRepository.findByUser_LoginIdAndKeyLogStatus("dusgh1234", KeyLogStatus.USE);
        assertThat(list.isEmpty()).isTrue();
        List<Notification> notifications = notificationRepository.findByReceiver_LoginId("dusgh12345");
        assertThat(notifications.isEmpty()).isTrue();
    }

}
