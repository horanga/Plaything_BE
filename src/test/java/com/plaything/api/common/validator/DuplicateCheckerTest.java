package com.plaything.api.common.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@SpringBootTest
public class DuplicateCheckerTest {


    @Autowired
    private DuplicateRequestChecker duplicateRequestChecker;

    @Autowired
    RedisTemplate<String, String> mockRedis;

    @BeforeEach
    void setUp(){
        Set<String> keys = mockRedis.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            mockRedis.delete(keys);
        }
    }

    @Test
    @DisplayName("Redis 정상 동작시 중복 요청 체크")
    void checkDuplicateRequest_Normal() {
        String userId = "testUser";
        String transactionId = "transaction123";

        duplicateRequestChecker.checkDuplicateRequest(userId, transactionId);

        boolean isDuplicate = duplicateRequestChecker.checkDuplicateRequest(userId, transactionId);
        assertThat(isDuplicate).isFalse();
    }
}
