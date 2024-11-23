package com.plaything.api.common.validator;

import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.lettuce.core.RedisCommandTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
public class CircuitBreakerTest {

    @SpyBean
    private DuplicateRequestChecker duplicateRequestChecker;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @SpyBean
    private PointKeyRepository pointKeyRepository;

    @SpyBean
    RedisTemplate<String, String> mockRedis;

    @BeforeEach
    void setUp(){
        Set<String> keys = mockRedis.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            mockRedis.delete(keys);
        }
    }

    @DisplayName("실패가 누적되면 서킷브레이커가 open이 된다")
    @Test
    void testCircuitBreakerStateTransition() {

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(SIMPLE_CIRCUIT_BREAKER_CONIFG);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(mockRedis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any()))
                .thenThrow(new RedisCommandTimeoutException("Redis is down"));

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // CircuitBreaker가 OPEN 상태가 될 때까지 충분한 실패 요청 발생
        for (int i = 0; i < 10; i++) {
            try {
                duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
            } catch (Exception ignored) {
            }
        }

        verify(duplicateRequestChecker, times(10)).fallback(any(), any(), any());
        verify(pointKeyRepository, times(10)).existsByTransactionId(any()); // 모든 요청이 fallback으로
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }

    @DisplayName("half-closed 상태에서 계속 실패하면 open상태가 된다.")
    @Test
    void testHalfOpenToOpen() throws InterruptedException {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(mockRedis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any()))
                .thenThrow(new RedisCommandTimeoutException("Redis is down"));

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(SIMPLE_CIRCUIT_BREAKER_CONIFG);

        for (int i = 0; i < 10; i++) {
            try {
                duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
            } catch (Exception ignored) {
            }
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 2. wait-duration-in-open-state 만큼 대기해서 HALF_OPEN으로 전환
        Thread.sleep(5000);


        for (int i = 0; i < 5; i++) {
            try {
                duplicateRequestChecker.checkDuplicateRequest("user" + i, "tx" + i);
            } catch (Exception ignored) {
            }
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

}
