package com.plaything.api.domain.key.service;

import com.plaything.api.domain.auth.client.dto.request.GoogleLoginRequest;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.repository.entity.log.KeyLog;
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
import java.util.List;
import java.util.Set;

import static com.plaything.api.domain.key.constant.KeyType.POINT_KEY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Transactional
@SpringBootTest
public class LoginPointKeyRollBackTest {

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

    @MockBean
    private PointKeyLogServiceV1 pointKeyLogServiceV1;

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

    @DisplayName("첫 로그인 후 체크 예외 발생 시 롤백된다")
    @Test
    void test3() throws IOException {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.createUser(request);
        LocalDate now = LocalDate.now();
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest("dusgh1234", "1234");

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        doThrow(IOException.class)
                .when(pointKeyLogServiceV1)
                .createLog(any(), any(), any(), any());


        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(0L);
        List<KeyLog> keyLogs = keyLogRepository.findByUser_LoginIdAndKeyType("dusgh1234", POINT_KEY);
        assertThat(keyLogs.size()).isEqualTo(0);
    }


    @DisplayName("첫 로그인 후 언체크 예외 발생 시 롤백된다")
    @Test
    void test4() throws IOException {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.createUser(request);
        LocalDate now = LocalDate.now();
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest("dusgh1234", "1234");

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        doThrow(RuntimeException.class)
                .when(pointKeyLogServiceV1)
                .createLog(any(), any(), any(), any());


        TestTransaction.flagForRollback();
        TestTransaction.end();
        TestTransaction.start();

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("dusgh1234");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(0L);
        List<KeyLog> keyLogs = keyLogRepository.findByUser_LoginIdAndKeyType("dusgh1234", POINT_KEY);
        assertThat(keyLogs.size()).isEqualTo(0);
    }

}
