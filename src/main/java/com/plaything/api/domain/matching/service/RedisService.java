package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static com.plaything.api.domain.matching.constants.MatchingConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MatchingServiceV1 matchingServiceV1;
    private final ProfileFacadeV1 profileFacadeV1;
    private final Map<String, int[]> viewCountMap = new ConcurrentHashMap<>();

    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "findMatchingCandidatesFallback")
    public List<UserMatching> findMatchingCandidates(String loginId, int duration, TimeUnit timeUnit) {
        String candidateKey = loginId + MATCHING_CANDIDATE_REDIS_KEY;
        String matchingKey = loginId + MATCHING_LIST_REDIS_KEY;
        String profileKey = loginId + LAST_PROFILE_ID_REDIS_KEY;
        String countKey = loginId + COUNT_REDIS_KEY;
        String profileHideKey = loginId +HIDE_PROFILE_KEY;

        List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.keyCommands().exists(candidateKey.getBytes());
            connection.keyCommands().exists(matchingKey.getBytes());
            connection.keyCommands().exists(profileHideKey.getBytes());
            connection.listCommands().lRange(candidateKey.getBytes(), 0, -1);
            connection.listCommands().lRange(matchingKey.getBytes(), 0, -1);
            connection.listCommands().lRange(profileHideKey.getBytes(), 0, -1);
            connection.stringCommands().get(profileKey.getBytes());
            connection.stringCommands().get(countKey.getBytes());
            return null;
        });

        boolean hasCandidates = (Boolean) results.get(0);  // exists는 Long 타입으로 반환
        boolean hasMatching = (Boolean) results.get(1);
        boolean hasHideProfile = (Boolean) results.get(2);
        List<String> candidateList = (List<String>) results.get(3);
        List<String> matchingList = (List<String>) results.get(4);
        List<String> hideList = (List<String>) results.get(5);
        String lastProfileId = (String) results.get(6);
        String count = (String) results.get(7);

        return getUserMatchingInfo(loginId,
                hasMatching,
                hasCandidates,
                hasHideProfile,
                candidateList,
                matchingList,
                hideList,
                lastProfileId,
                count,
                duration,
                timeUnit);
    }

    public List<UserMatching> findMatchingCandidatesFallback(String loginId, int duration, TimeUnit timeUnit, Exception ex) {
        logError(ex);
        List<String> candidates = matchingServiceV1.getMatchingCandidate(loginId);
        List<String> partners = matchingServiceV1.getMatchingPartner(loginId);
        List<String> hideList = profileFacadeV1.getHideList(loginId);

        if (!viewCountMap.containsKey(loginId)) {
            return matchingServiceV1.searchPartner(loginId, candidates, partners, hideList, 0);
        }
        int[] counts = viewCountMap.get(loginId);
        List<String> availableCandidate = getAvailableCandidates(candidates, counts[0] / MAX_SKIP_COUNT);
        return matchingServiceV1.searchPartner(loginId, availableCandidate, partners, hideList, counts[1]);
    }


    @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "handleViewCountError")
    public void updateViewCount(String loginId, Long profileId, int countDuration, int profileIdDuration, TimeUnit timeUnit) {
        String countKey = loginId + COUNT_REDIS_KEY;
        String profileKey = loginId + LAST_PROFILE_ID_REDIS_KEY;

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.stringCommands().incr(countKey.getBytes());
            connection.keyCommands().expire(countKey.getBytes(), timeUnit.toSeconds(countDuration));
            connection.stringCommands().setEx(profileKey.getBytes(),
                    timeUnit.toSeconds(profileIdDuration),
                    String.valueOf(profileId).getBytes());
            return null;
        });
    }

    public void handleViewCountError(String loginId, Long profileId, int countDuration, int profileIdDuration, TimeUnit timeUnit, Exception ex) {
        logError(ex);
        int[] count = viewCountMap.computeIfAbsent(loginId, k -> new int[]{0, 0});
        count[0]++; //count
        count[1]++; //profileId
    }

    private List<UserMatching> getUserMatchingInfo(
            String loginId,
            boolean hasMatching,
            boolean hasCandidates,
            boolean hasHideProfile,
            List<String> candidateList,
            List<String> matchingList,
            List<String> hideList,
            String lastProfileId,
            String count,
            int duration,
            TimeUnit timeUnit) {


        if (!hasMatching || !hasCandidates || !hasHideProfile) {
            return refreshCacheFromDB(
                    hasCandidates,
                    hasMatching,
                    hasHideProfile,
                    candidateList,
                    matchingList,
                    hideList,
                    lastProfileId,
                    loginId,
                    duration,
                    timeUnit,
                    count);
        }

        List<String> candidates = getAvailableCandidates(candidateList, count == null ? 0 : Integer.parseInt(count) / MAX_SKIP_COUNT);

        return matchingServiceV1.searchPartner(
                loginId,
                candidates,
                matchingList,
                hideList,
                lastProfileId != null ? Long.parseLong(lastProfileId) : 0L
        );
    }

    private List<String> getAvailableCandidates(List<String> candidateList, int skipTimes) {
        if (skipTimes > candidateList.size()) {
            return Collections.emptyList();
        }
        return candidateList.subList(skipTimes, candidateList.size());
    }

    private List<UserMatching> refreshCacheFromDB(
            boolean candidateExist,
            boolean matchingExist,
            boolean hasHideProfile,
            List<String> candidateList,
            List<String> matchingList,
            List<String> hideList,
            String lastProfileId,
            String loginId,
            int duration,
            TimeUnit timeUnit, String count) {

        if (!candidateExist) {
            candidateList = matchingServiceV1.getMatchingCandidate(loginId);
            cacheListWithExpiry(loginId + MATCHING_CANDIDATE_REDIS_KEY, candidateList, duration, timeUnit);
        }

        if (!matchingExist) {
            matchingList = matchingServiceV1.getMatchingPartner(loginId);
            cacheListWithExpiry(loginId + MATCHING_LIST_REDIS_KEY, matchingList, duration, timeUnit);
        }

        if(!hasHideProfile){
            hideList = profileFacadeV1.getHideList(loginId);
            cacheListWithExpiry(loginId+HIDE_PROFILE_KEY, hideList, duration, timeUnit);
        }

        List<String> candidates = getAvailableCandidates(candidateList, count == null ? 0 : Integer.parseInt(count) / MAX_SKIP_COUNT);

        return matchingServiceV1.searchPartner(
                loginId,
                candidates,
                matchingList,
                hideList,
                lastProfileId != null ? Long.parseLong(lastProfileId) : 0L);
    }

    private void cacheListWithExpiry(String key, List<String> values, int duration, TimeUnit timeUnit) {
        if (values.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, List.of(KEYWORD_DUMMY_CACHE));
            redisTemplate.expire(key, duration, timeUnit);
        } else {
            redisTemplate.opsForList().rightPushAll(key, values);
            redisTemplate.expire(key, duration, timeUnit);
        }
    }


    private void logError(Exception ex) {
        log.error("Redis check failed, using DB fallback. Exception type: {}, Message: {}",
                ex.getClass().getName(), ex.getMessage());
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupViewCountMap() {
        viewCountMap.clear();
        log.info("View count map cleared");
    }
}
