package com.plaything.api.domain.key.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.client.dto.request.LoginRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.matching.service.MatchingFacadeV1;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import com.plaything.api.domain.repository.entity.notification.Notification;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.log.KeyLogRepository;
import com.plaything.api.domain.repository.repo.monitor.ProfileImagesRegistrationRepository;
import com.plaything.api.domain.repository.repo.monitor.ProfileRecordRepository;
import com.plaything.api.domain.repository.repo.notification.NotificationRepository;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.pay.UserRewardActivityRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.PersonalityTraitRepository;
import com.plaything.api.domain.repository.repo.user.RelationshipRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.util.UserGenerator;
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
import java.util.List;
import java.util.Set;

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
    private UserGenerator userGenerator;
    @Autowired
    private PointKeyRepository pointKeyRepository;

    @Autowired
    private ProfileImagesRegistrationRepository profileImagesRegistrationRepository;

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    private ProfileRecordRepository profileRecordRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RelationshipRepository relationshipRepository;

    @Autowired
    private PersonalityTraitRepository personalityTraitRepository;

    @Autowired
    private UserRewardActivityRepository userRewardActivityRepository;


    @BeforeEach
    void setUp() {

        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }

        userGenerator.generate("dusgh1234", "1234", "1", "알렉1");
        userGenerator.addImages("알렉1", "abc", true);

        userGenerator.generate("dusgh12345", "1234", "1", "알렉2");
        userGenerator.addImages("알렉2", "abc", true);

    }

    @AfterEach
    void cleanup() {

        // 자식 테이블(참조하는 테이블)부터 삭제
        keyLogRepository.deleteAllInBatch();        // point_key를 참조하는 key_log 먼저 삭제
        pointKeyRepository.deleteAllInBatch();      // 그 다음 point_key 삭제

        // 나머지 테이블들도 참조 관계를 고려해서 순서대로 삭제
        profileImagesRegistrationRepository.deleteAllInBatch();
        profileImageRepository.deleteAllInBatch();
        personalityTraitRepository.deleteAllInBatch();
        relationshipRepository.deleteAllInBatch();
        profileRecordRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        profileRepository.deleteAllInBatch();

        TestTransaction.flagForCommit();
        TestTransaction.end();

    }

    @DisplayName("포인트 키 사용중 체크 예외가 발생하면 키 사용 이력이 롤백된다")
    @Test
    void test1() throws IOException {

        LoginRequest request = new LoginRequest("dusgh1234", "1234");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();


        AvailablePointKey availablePointKey = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");

        doThrow(new IOException())
                .when(notificationServiceV1)
                .saveNotification(any(), any(), any());

        assertThatThrownBy(() -> matchingFacadeV1.sendMatchingRequest("dusgh1234", new MatchingRequest("dusgh12345"), "123"))
                .isInstanceOf(CustomException.class);


        // 현재 트랜잭션을 롤백하고 종료
        TestTransaction.flagForRollback();
        TestTransaction.end();

        // 새로운 트랜잭션에서 결과 확인
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
        assertThatThrownBy(() -> matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, "123"))
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
