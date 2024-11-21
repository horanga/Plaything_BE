package com.plaything.api.common.validator;

import com.plaything.api.TestRedisConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(TestRedisConfig.class)
@SpringBootTest
public class DuplicateTest2 {

    @SpyBean
    RedisTemplate<String, String> mockRedis;

    @Autowired
    DuplicateRequestChecker duplicateRequestChecker;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void testHalfOpenToClosed() throws InterruptedException {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(SIMPLE_CIRCUIT_BREAKER_CONIFG);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(mockRedis.opsForValue()).thenReturn(valueOperations);

        // 슬로우콜로 OPEN 상태 만들기
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(2000); // slow-call-duration-threshold보다 길게
                    return true;
                });

        // OPEN 상태 만들기
        for (int i = 0; i < 10; i++) {
            duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(4000);

        // 성공 케이스
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any()))
                .thenReturn(true);

        // HALF_OPEN에서 permitted-number-of-calls-in-half-open-state만큼 성공
        for (int i = 0; i < 5; i++) {
            duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
