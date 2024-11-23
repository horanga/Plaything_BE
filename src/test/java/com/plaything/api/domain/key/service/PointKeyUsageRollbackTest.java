package com.plaything.api.domain.key.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import com.plaything.api.domain.repository.entity.notification.Notification;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.log.KeyLogRepository;
import com.plaything.api.domain.repository.repo.notification.NotificationRepository;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.pay.UserRewardActivityRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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

    @AfterEach
    void cleanup() {
        keyLogRepository.deleteAll();
        pointKeyRepository.deleteAll();
        userRewardActivityRepository.deleteAll();
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
        MatchingRequest matchingRequest = new MatchingRequest(user.getLoginId());
        assertThatThrownBy(() -> pointKeyFacadeV1.usePointKeyForMatching("dusgh12345", matchingRequest, "1"))
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
        MatchingRequest matchingRequest = new MatchingRequest(user.getLoginId());
        assertThatThrownBy(() -> pointKeyFacadeV1.usePointKeyForMatching("dusgh12345", matchingRequest, "1"))
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

}
