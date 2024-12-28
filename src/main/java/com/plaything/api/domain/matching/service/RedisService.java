package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.matching.model.response.UserMatching;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static com.plaything.api.domain.matching.constants.MatchingConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MatchingServiceV1 matchingServiceV1;

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "searchFallback")
    public List<UserMatching> searchMatchingPartner(String loginId, int duration, TimeUnit timeUnit) {
        String candidateKey = loginId + MATCHING_CANDIDATE_REDIS_KEY;
        String matchingKey = loginId + MATCHING_LIST_REDIS_KEY;
        String profileKey = loginId + LAST_PROFILE_ID_REDIS_KEY;

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.keyCommands().exists(candidateKey.getBytes());
            connection.keyCommands().exists(matchingKey.getBytes());
            connection.listCommands().lRange(candidateKey.getBytes(), 0, -1);
            connection.listCommands().lRange(matchingKey.getBytes(), 0, -1);
            connection.stringCommands().get(profileKey.getBytes());

            return null;
        });

        boolean hasCandidates = (Boolean) results.get(0);  // exists는 Long 타입으로 반환
        boolean hasMatching = (Boolean) results.get(1);
        List<String> candidateList = (List<String>) results.get(2);
        List<String> matchingList = (List<String>) results.get(3);
        String lastProfileId = (String) results.get(4);

        return getUserMatchingInfo(loginId,
                hasMatching,
                hasCandidates,
                candidateList,
                matchingList,
                lastProfileId,
                duration,
                timeUnit);
    }

    public List<UserMatching> searchFallback(String loginId, int duration, TimeUnit timeUnit, Exception ex) {
        logError(ex);
        List<String> matchingCandidate = matchingServiceV1.getMatchingCandidate(loginId);
        List<String> matchingPartner = matchingServiceV1.getMatchingPartner(loginId);
        return matchingServiceV1.searchPartner(loginId, matchingCandidate, matchingPartner, 0);
    }

    private List<UserMatching> getUserMatchingInfo(
            String loginId,
            boolean hasMatching,
            boolean hasCandidates,
            List<String> candidateList,
            List<String> matchingList,
            String lastProfileId,
            int duration,
            TimeUnit timeUnit) {

        if (!hasMatching || !hasCandidates) {
            return findFromDBAndSetCache(
                    hasCandidates,
                    hasMatching,
                    candidateList,
                    matchingList,
                    lastProfileId,
                    loginId,
                    duration,
                    timeUnit);
        }

        return matchingServiceV1.searchPartner(
                loginId,
                candidateList,
                matchingList,
                lastProfileId != null ? Long.parseLong(lastProfileId) : 0L
        );
    }


    private List<UserMatching> findFromDBAndSetCache(
            boolean candidateExist,
            boolean matchingExist,
            List<String> candidateList,
            List<String> matchingList,
            String lastProfileId,
            String loginId,
            int duration,
            TimeUnit timeUnit) {

        if (!candidateExist) {
            candidateList = matchingServiceV1.getMatchingCandidate(loginId);
            cacheList(loginId + MATCHING_CANDIDATE_REDIS_KEY, candidateList, duration, timeUnit);
        }

        if (!matchingExist) {
            matchingList = matchingServiceV1.getMatchingPartner(loginId);
            cacheList(loginId + MATCHING_LIST_REDIS_KEY, matchingList, duration, timeUnit);
        }

        return matchingServiceV1.searchPartner(
                loginId,
                candidateList,
                matchingList,
                lastProfileId != null ? Long.parseLong(lastProfileId) : 0L);
    }

    private void cacheList(String key, List<String> values, int duration, TimeUnit timeUnit) {
        if (values.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, List.of(KEYWORD_DUMMY_CACHE));
            redisTemplate.expire(key, duration, timeUnit);
        } else {
            redisTemplate.opsForList().rightPushAll(key, values);
            redisTemplate.expire(key, duration, timeUnit);
        }
    }

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "redisError")
    public void incrementSkipCount(String key, Long profileId, int countDuration, int profileIdDuration, TimeUnit timeUnit) {
        String countKey = key + COUNT_REDIS_KEY;
        redisTemplate.opsForValue().increment(countKey);
        String profileKey = key + LAST_PROFILE_ID_REDIS_KEY;
        redisTemplate.expire(countKey, countDuration, timeUnit);

        redisTemplate.opsForValue().set(profileKey, String.valueOf(profileId), profileIdDuration, timeUnit);
    }

    public void redisError(String key, Long profileId, int countDuration, int profileIdDuration, TimeUnit timeUnit, Exception ex) {
        logError(ex);
    }

    public long getCount(String loginId) {
        String key = loginId + COUNT_REDIS_KEY;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }


    // 조회할 때 7일 이상 지난 항목은 제외
    public List<String> getActiveBlacklistedUsers(String key, LocalDateTime now) {
        String redisKey = key + "::blackList";
        List<String> allItems = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (allItems == null) {
            return Collections.emptyList();
        }

        LocalDate weekAgo = now.toLocalDate().minusDays(RE_MATCHING_PERIOD);

        // 유효한 항목과 만료된 항목 분리
        List<String> validItems = new ArrayList<>();
        List<String> expiredItems = new ArrayList<>();

        for (String item : allItems) {
            String[] parts = item.split(":");
            LocalDate blacklistDate = LocalDate.parse(parts[1]);
            if (blacklistDate.isAfter(weekAgo)) {
                validItems.add(parts[0]);  // 유효한 loginId 저장
            } else {
                expiredItems.add(item);    // 만료된 항목 저장
            }
        }

        // 만료된 항목들 삭제
        for (String expired : expiredItems) {
            redisTemplate.opsForList().remove(redisKey, 1, expired);
        }

        return validItems;
    }

    public void addToBlackList(String key, LocalDate now, String loginId) {
        String redisKey = key + "::blackList";
        // loginId:등록날짜 형태로 저장
        String value = loginId + ":" + now.toString();
        redisTemplate.opsForList().rightPush(redisKey, value);
    }

    private void logError(Exception ex) {
        log.error("Redis check failed, using DB fallback. Exception type: {}, Message: {}",
                ex.getClass().getName(), ex.getMessage());
    }
}
