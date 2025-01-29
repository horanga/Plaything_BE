package com.plaything.api.domain.matching.service;

import static com.plaything.api.common.validator.DuplicateRequestChecker.SIMPLE_CIRCUIT_BREAKER_CONIFG;
import static com.plaything.api.domain.matching.constants.MatchingConstants.COUNT_REDIS_KEY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.HIDE_PROFILE_KEY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.KEYWORD_DUMMY_CACHE;
import static com.plaything.api.domain.matching.constants.MatchingConstants.LAST_PROFILE_ID_REDIS_KEY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.MATCHING_CANDIDATE_REDIS_KEY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.MATCHING_LIST_REDIS_KEY;
import static com.plaything.api.domain.matching.constants.MatchingConstants.MAX_SKIP_COUNT;

import com.plaything.api.domain.matching.model.response.MatchingData;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

  private final RedisTemplate<String, String> redisTemplate;
  private final MatchingServiceV1 matchingServiceV1;
  private final ProfileFacadeV1 profileFacadeV1;
  private final Map<String, int[]> viewCountMap = new ConcurrentHashMap<>();
  // int[0] count
  // int[1] profileId

  @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "redisCacheFallback")
  public MatchingData getRedisCache(String loginId) {
    String candidateKey = loginId + MATCHING_CANDIDATE_REDIS_KEY;
    String matchingKey = loginId + MATCHING_LIST_REDIS_KEY;
    String profileKey = loginId + LAST_PROFILE_ID_REDIS_KEY;
    String countKey = loginId + COUNT_REDIS_KEY;
    String profileHideKey = loginId + HIDE_PROFILE_KEY;

    List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
      connection.listCommands().lRange(candidateKey.getBytes(), 0, -1);
      connection.listCommands().lRange(matchingKey.getBytes(), 0, -1);
      connection.listCommands().lRange(profileHideKey.getBytes(), 0, -1);
      connection.stringCommands().get(profileKey.getBytes());
      connection.stringCommands().get(countKey.getBytes());
      return null;
    });

    List<String> candidateList = (List<String>) results.get(0);
    List<String> matchingList = (List<String>) results.get(1);
    List<String> hideList = (List<String>) results.get(2);
    String lastProfileId = (String) results.get(3);
    String count = (String) results.get(4);

    return new MatchingData(candidateList, matchingList, hideList, lastProfileId, count, true);
  }

  public MatchingData redisCacheFallback(String loginId, Exception ex) {
    logError(ex);
    List<String> candidates = matchingServiceV1.getMatchingCandidate(loginId);
    List<String> partners = matchingServiceV1.getMatchingPartner(loginId);
    List<String> hideList = profileFacadeV1.getHideList(loginId);

    if (!viewCountMap.containsKey(loginId)) {
      return new MatchingData(candidates, partners, hideList, "0", "0", false);
    }
    int[] info = viewCountMap.get(loginId);
    List<String> availableCandidate = getAvailableCandidates(candidates, info[0] / MAX_SKIP_COUNT);
    return new MatchingData(availableCandidate, partners, hideList, String.valueOf(info[1]),
        String.valueOf(info[0]), false);
  }


  @CircuitBreaker(name = SIMPLE_CIRCUIT_BREAKER_CONIFG, fallbackMethod = "handleViewCountError")
  public void updateViewCount(String loginId, Long profileId, int countDuration,
      int profileIdDuration, TimeUnit timeUnit) {
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

  public void handleViewCountError(String loginId, Long profileId, int countDuration,
      int profileIdDuration, TimeUnit timeUnit, Exception ex) {
    logError(ex);
    int[] count = viewCountMap.computeIfAbsent(loginId, k -> new int[]{0, 0});
    count[0]++; //count
    count[1]++; //profileId
  }

  public List<UserMatching> getUserMatchingInfo(MatchingData cache,
      String loginId,
      int duration,
      TimeUnit timeUnit) {

    List<String> candidateList = cache.candidateList();
    List<String> matchingList = cache.matchingList();
    List<String> hideList = cache.hideList();
    String lastProfileId = cache.lastProfileId();
    String count = cache.count();

    if (cache.isAvailable()) {

      if (candidateList.isEmpty() || matchingList.isEmpty() || hideList.isEmpty()) {
        return refreshCacheFromDB(
            candidateList,
            matchingList,
            hideList,
            lastProfileId,
            loginId,
            duration,
            timeUnit,
            count);
      }
    }

    List<String> candidates = getAvailableCandidates(candidateList,
        count == null ? 0 : Integer.parseInt(count) / MAX_SKIP_COUNT);

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
      List<String> candidateList,
      List<String> matchingList,
      List<String> hideList,
      String lastProfileId,
      String loginId,
      int duration,
      TimeUnit timeUnit, String count) {

    if (candidateList.isEmpty()) {
      candidateList = matchingServiceV1.getMatchingCandidate(loginId);
      cacheListWithExpiry(loginId + MATCHING_CANDIDATE_REDIS_KEY, candidateList, duration,
          timeUnit);
    }

    if (matchingList.isEmpty()) {
      matchingList = matchingServiceV1.getMatchingPartner(loginId);
      cacheListWithExpiry(loginId + MATCHING_LIST_REDIS_KEY, matchingList, duration, timeUnit);
    }

    if (hideList.isEmpty()) {
      hideList = profileFacadeV1.getHideList(loginId);
      cacheListWithExpiry(loginId + HIDE_PROFILE_KEY, hideList, duration, timeUnit);
    }

    List<String> candidates = getAvailableCandidates(candidateList,
        count == null ? 0 : Integer.parseInt(count) / MAX_SKIP_COUNT);

    return matchingServiceV1.searchPartner(
        loginId,
        candidates,
        matchingList,
        hideList,
        lastProfileId != null ? Long.parseLong(lastProfileId) : 0L);
  }

  private void cacheListWithExpiry(String key, List<String> values, int duration,
      TimeUnit timeUnit) {
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
