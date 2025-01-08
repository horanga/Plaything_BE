package com.plaything.api.common.validator;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Set;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class DuplicateTest2 {

    @SpyBean
    RedisTemplate<String, String> mockRedis;

    @Autowired
    DuplicateRequestChecker duplicateRequestChecker;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        Set<String> keys = mockRedis.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            mockRedis.delete(keys);
        }
    }

    @Test
    void testHalfOpenToClosed() throws InterruptedException {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(SIMPLE_CIRCUIT_BREAKER_CONIFG);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(mockRedis.opsForValue()).thenReturn(valueOperations);

        // 슬로우콜로 OPEN 상태 만들기
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(6000); // slow-call-duration-threshold보다 길게
                    return true;
                });

        // OPEN 상태 만들기
        for (int i = 0; i < 10; i++) {
            duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(7000);

        // HALF_OPEN 상태에서의 성공 케이스 설정
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any()))
                .thenReturn(true);

        // permitted-number-of-calls-in-half-open-state(5)만큼 성공 케이스 실행
        for (int i = 0; i < 5; i++) {
            duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
