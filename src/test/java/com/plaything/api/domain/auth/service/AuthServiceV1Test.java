package com.plaything.api.domain.auth.service;

import com.plaything.api.TestRedisConfig;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.request.LoginRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.plaything.api.common.exception.ErrorCode.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

@Import(TestRedisConfig.class)
@Transactional
@SpringBootTest
class AuthServiceV1Test {

    @Autowired
    private AuthServiceV1 authServiceV1;


    @Autowired
    protected RedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {

        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        if (connectionFactory != null) {
            RedisConnection connection = connectionFactory.getConnection();
            connection.serverCommands().flushAll(); // 모든 데이터를 초기화
        }
        CreateUserRequest request = new CreateUserRequest("fnel123", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("fnel1234", "1234", "1");
        authServiceV1.creatUser(request2);
    }

    @DisplayName("첫 로그인을 하면 Point key 1개를 받는다.")
    @Test
    void test1() {
        LoginRequest request = new LoginRequest("fnel123", "1234");
        LoginResponse login = authServiceV1.login(request, LocalDate.now(), "1");

        assertThat(login.dailyRewardProvided()).isTrue();
        assertThat(login.invalidProfile()).isTrue();
        assertThat(login.description()).isEqualTo(SUCCESS);

    }
}