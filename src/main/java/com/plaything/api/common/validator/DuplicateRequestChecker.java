package com.plaything.api.common.validator;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class DuplicateRequestChecker {

    private final RedisTemplate<String, String> redisTemplate;
    private final PointKeyRepository pointKeyRepository;

    public static final String SIMPLE_CIRCUIT_BREAKER_CONIFG = "simpleCircuitBreakerConfig";

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "fallback")
    public boolean checkDuplicateRequest(String userId, String transactionId) {


        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(userId + ":" + transactionId, "success", 10, TimeUnit.SECONDS));
    }

    public boolean fallback(String userId, String transactionId, Exception ex) {
        log.error("Redis check failed, using DB fallback: {}", ex.getMessage());

        return !pointKeyRepository.existsByTransactionId(transactionId);
    }
}
