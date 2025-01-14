package com.plaything.api.common.validator;

import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class DuplicateRequestChecker {

    private final RedisTemplate<String, String> redisTemplate;
    private final PointKeyRepository pointKeyRepository;
    private final Map<String, String> imageRequestMap = new ConcurrentHashMap<>();

    public static final String SIMPLE_CIRCUIT_BREAKER_CONIFG = "simpleCircuitBreakerConfig";

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "fallback")
    public boolean checkDuplicateRequest(String userId, String transactionId) {

        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(userId + ":" + transactionId, "success", 20, TimeUnit.SECONDS));
    }

    public boolean fallback(String userId, String transactionId, Exception ex) {
        log.error("Redis check failed, using DB fallback. Exception type: {}, Message: {}",
                ex.getClass().getName(), ex.getMessage());

        return !pointKeyRepository.existsByTransactionId(transactionId);
    }


    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "imageFallback")
    public boolean checkImageDuplicateRequest(String userId, String transactionId) {

        //중복이면 false
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(userId + "::" + transactionId + ",Image", "success", 15, TimeUnit.SECONDS));
    }

    public boolean imageFallback(String userId, String transactionId, Exception ex) {
        log.error("Redis check failed, using DB fallback. Exception type: {}, Message: {}",
                ex.getClass().getName(), ex.getMessage());

        if (!imageRequestMap.containsKey(userId + transactionId)) {
            imageRequestMap.put(userId, transactionId);
            return true;
        } else {
            return false;
        }
    }

    @Scheduled(cron = "0 0 */3 * * *")
    public void cleanupMap() {
        imageRequestMap.clear();
        log.info("image duplicate request 데이터 삭제");
    }

}
