package com.plaything.api.domain.key.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.client.dto.request.LoginRequest;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.repository.entity.log.AdViewLog;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import com.plaything.api.domain.repository.repo.log.AdViewLogRepository;
import com.plaything.api.domain.repository.repo.log.KeyLogRepository;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.pay.UserRewardActivityRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.plaything.api.domain.key.constant.KeySource.LOGIN_REWARD;
import static com.plaything.api.domain.key.constant.KeyType.POINT_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Transactional
@SpringBootTest
public class AdPointKeyRollbackTest {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public AuthServiceV1 authServiceV1;

    @Autowired
    public PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    public UserRewardActivityRepository userRewardActivityRepository;

    @Autowired
    private KeyLogRepository keyLogRepository;

    @Autowired
    private PointKeyRepository pointKeyRepository;

    @Autowired
    private AdViewLogRepository adViewLogRepository;

    @MockBean
    public AdLogServiceV1 adLogServiceV1;

    @Autowired
    protected RedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {

        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }

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

    @DisplayName("광고 시청후 포인트 키가 지급된 후 체크 예외 발생 시 롤백된다")
    @Test
    void test1() throws IOException {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);
        LocalDate now = LocalDate.now();
        LoginRequest loginRequest = new LoginRequest("dusgh1234", "1234");

        // 첫 번째 트랜잭션 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 새로운 트랜잭션 시작
        TestTransaction.start();

        AdRewardRequest adRequest = new AdRewardRequest("광고1", 2);
        doThrow(new IOException())
                .when(adLogServiceV1)
                .createAdViewLog(any(), any());
        // 여기서 체크 예외가 발생하고 롤백되어야 함
        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", adRequest, LocalDateTime.now(), "1"))
                .isInstanceOf(CustomException.class);
        // 명시적으로 롤백 플래그 설정
        TestTransaction.flagForRollback();
        TestTransaction.end();

        // 새로운 트랜잭션에서 검증
        TestTransaction.start();

        // 포인트 키가 롤백되었는지 확인
        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(1L);
        List<AdViewLog> adViewLogs = adViewLogRepository.findByUser_LoginId("dusgh1234");
        assertThat(adViewLogs.size()).isEqualTo(0);
        List<KeyLog> keyLogs = keyLogRepository.findByUser_LoginIdAndKeyType("dusgh1234", POINT_KEY);
        assertThat(keyLogs.size()).isEqualTo(1);
        assertThat(keyLogs.get(0).getKeySource()).isEqualTo(LOGIN_REWARD);
    }


    @DisplayName("광고 시청후 포인트 키가 지급된 후 언체크 예외 발생 시 롤백된다")
    @Test
    void test2() throws IOException {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);
        LocalDate now = LocalDate.now();
        LoginRequest loginRequest = new LoginRequest("dusgh1234", "1234");

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        AdRewardRequest adRequest = new AdRewardRequest("광고1", 2);
        doThrow(RuntimeException.class)
                .when(adLogServiceV1)
                .createAdViewLog(any(), any());
        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", adRequest, LocalDateTime.now(), "1"))
                .isInstanceOf(RuntimeException.class);

        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(1L);
        List<AdViewLog> adViewLogs = adViewLogRepository.findByUser_LoginId("dusgh1234");
        assertThat(adViewLogs.size()).isEqualTo(0);
        List<KeyLog> keyLogs = keyLogRepository.findByUser_LoginIdAndKeyType("dusgh1234", POINT_KEY);
        assertThat(keyLogs.size()).isEqualTo(1);
        assertThat(keyLogs.get(0).getKeySource()).isEqualTo(LOGIN_REWARD);
    }

}
