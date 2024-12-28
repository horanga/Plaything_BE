package com.plaything.api.domain.matching.service;


import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.util.UserGenerator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.lettuce.core.RedisConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static com.plaything.api.domain.matching.constants.MatchingConstants.CACHE_DURATION_DAY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.CACHE_DURATION_UNIT_DAYS;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.PREY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
public class RedisServiceCircuitBreakerTest {

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @SpyBean
    private RedisService redisService;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;

    @BeforeEach
    void setup() {
        // 각 테스트 전에 CircuitBreaker 상태 초기화
        circuitBreakerRegistry.circuitBreaker(SIMPLE_CIRCUIT_BREAKER_CONIFG).reset();
        userGenerator.generateWithRole("fnel123", "123", "1", "연호1", PrimaryRole.TOP, PersonalityTraitConstant.HUNTER);
        userGenerator.addImages("연호1", "dd");

        userGenerator.generateWithRole("fnel1234", "123", "1", "연호2", PrimaryRole.BOTTOM, PREY);
        userGenerator.addImages("연호2", "dd");

        userGenerator.generateWithRole("fnel12345", "123", "1", "연호3", PrimaryRole.BOTTOM, PREY);
        userGenerator.addImages("연호3", "dd");

        userGenerator.generateWithRole("fnel123456", "123", "1", "연호4", PrimaryRole.BOTTOM, PREY);
        userGenerator.addImages("연호4", "dd");

        userGenerator.generateWithRole("fnel1234567", "123", "1", "연호5", PrimaryRole.BOTTOM, PREY);
        userGenerator.addImages("연호5", "dd");


    }

    @DisplayName("매칭 파트너를 찾을 때 레디스에 문제가 있으면 fallback 매커니즘이 작동한다")
    @Test
    void test1() {

        // Given
        when(redisTemplate.executePipelined(any(RedisCallback.class)))
                .thenThrow(new RedisConnectionException("Redis Connection Error"));

        // When
        List<UserMatching> list = matchingFacadeV1.searchMatchingPartner(
                "fnel123", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);

        // Then
        verify(redisService).searchFallback(eq("fnel123"), eq(CACHE_DURATION_DAY), eq(CACHE_DURATION_UNIT_DAYS), any());
        assertThat(list).extracting("loginId").containsExactly("fnel1234", "fnel12345", "fnel123456", "fnel1234567");
    }

    @DisplayName("매칭 파트너 조회 에러가 반복되면 서킷브레이커가 open이 된다.")
    @Test
    void test3() {
        // Given
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(SIMPLE_CIRCUIT_BREAKER_CONIFG);

        when(redisTemplate.executePipelined(any(RedisCallback.class)))
                .thenThrow(new RedisConnectionException("Redis Connection Error"));

        // When: 임계치까지 에러 발생시키기
        for (int i = 0; i < 10; i++) { // 설정된 임계치만큼 반복
            matchingFacadeV1.searchMatchingPartner("fnel123", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);
        }

        // Then
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        verify(redisService, atLeast(5)).searchFallback(eq("fnel123"), eq(CACHE_DURATION_DAY), eq(CACHE_DURATION_UNIT_DAYS), any());
    }

}
