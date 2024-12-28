package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.plaything.api.domain.matching.constants.MatchingConstants.*;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.SERVANT;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class RedisServiceTest {

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;

    @BeforeEach
    void setUp() {
        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (!keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }

        userGenerator.generateWithRole("fnel1", "1234", "11", "연호1", PrimaryRole.TOP, PersonalityTraitConstant.BOSS);
        userGenerator.addImages("연호1", "abc");
        userGenerator.generateWithRole("fnel2", "1234", "11", "연호2", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호2", "abc");
        userGenerator.generateWithRole("fnel3", "1234", "11", "연호3", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호3", "abc");
        userGenerator.generateWithRole("fnel4", "1234", "11", "연호4", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호4", "abc");

        userGenerator.createPointKey("fnel1", 10);
    }

    @DisplayName("매칭 가능한 후보가 레디스에 캐싱된다")
    @Test
    void test1() {

        userGenerator.requestMatching("fnel1", "1234", "fnel2");

        List<UserMatching> matchingCandidate2 = matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);
        assertThat(matchingCandidate2).extracting("loginId").containsExactly("fnel3", "fnel4");

        Boolean hasKey = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        assertThat(hasKey).isTrue();
        Boolean hasKey2 = redisTemplate.hasKey("fnel1" + MATCHING_LIST_REDIS_KEY);
        assertThat(hasKey2).isTrue();


        List<String> cachedList = redisTemplate.opsForList().range("fnel1" + MATCHING_CANDIDATE_REDIS_KEY, 0, -1);
        assertThat(cachedList).containsExactly("fnel2");
        List<String> cachedList2 = redisTemplate.opsForList().range("fnel1" + MATCHING_LIST_REDIS_KEY, 0, -1);
        assertThat(cachedList2).containsExactly(KEYWORD_DUMMY_CACHE);
    }

    @DisplayName("매칭 리스트가 레디스에 캐싱된다")
    @Test
    void test2() {

        userGenerator.createMatching("fnel1", "1234", "fnel2", "1234");
        List<UserMatching> list = matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);
        assertThat(list).extracting("loginId").containsExactly("fnel3", "fnel4");

        Boolean hasKey = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        assertThat(hasKey).isTrue();
        Boolean hasKey2 = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        assertThat(hasKey2).isTrue();

        List<String> cachedList2 = redisTemplate.opsForList().range("fnel1" + MATCHING_CANDIDATE_REDIS_KEY, 0, -1);
        assertThat(cachedList2).containsExactly(KEYWORD_DUMMY_CACHE);
        List<String> cachedList = redisTemplate.opsForList().range("fnel1" + MATCHING_LIST_REDIS_KEY, 0, -1);
        assertThat(cachedList).containsExactly("fnel2");
    }

    @DisplayName("리스트가 없으면 레디스에 dummy 데이터가 저장된다.")
    @Test
    void test3() {

        Boolean hasKey1 = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        Boolean hasKey2 = redisTemplate.hasKey("fnel1" + MATCHING_LIST_REDIS_KEY);
        assertThat(hasKey1).isFalse();
        assertThat(hasKey2).isFalse();

        matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);
        Boolean hasKey3 = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        Boolean hasKey4 = redisTemplate.hasKey("fnel1" + MATCHING_LIST_REDIS_KEY);
        assertThat(hasKey3).isTrue();
        assertThat(hasKey4).isTrue();

        List<String> cachedList1 = redisTemplate.opsForList().range("fnel1" + MATCHING_CANDIDATE_REDIS_KEY, 0, -1);
        List<String> cachedList2 = redisTemplate.opsForList().range("fnel1" + MATCHING_LIST_REDIS_KEY, 0, -1);
        assertThat(cachedList1).containsExactly(KEYWORD_DUMMY_CACHE);
        assertThat(cachedList2).containsExactly(KEYWORD_DUMMY_CACHE);
    }


    @DisplayName("레디스에 저장된 캐시는 지정된 시간만큼만 저장된다")
    @Test
    void test4() throws InterruptedException {

        userGenerator.requestMatching("fnel1", "1234", "fnel2");
        userGenerator.requestMatching("fnel1", "1234", "fnel3");


        List<UserMatching> matchingCandidate2 = matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, TimeUnit.SECONDS);
        assertThat(matchingCandidate2).extracting("loginId").containsExactly("fnel4");

        Boolean hasKey = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        assertThat(hasKey).isTrue();
        List<String> cachedList = redisTemplate.opsForList().range("fnel1" + MATCHING_CANDIDATE_REDIS_KEY, 0, -1);
        assertThat(cachedList).containsExactly("fnel2", "fnel3");

        Thread.sleep(3000);
        Boolean hasKey2 = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        assertThat(hasKey2).isFalse();
    }

    @DisplayName("매칭 파트너를 조회할 땐 매칭 후보, 리스트가 제외된다")
    @Test
    void test5() {

        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호5", "abc");
        userGenerator.generateWithRole("fnel6", "1234", "11", "연호6", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호6", "abc");
        userGenerator.generateWithRole("fnel7", "1234", "11", "연호7", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호7", "abc");
        userGenerator.generateWithRole("fnel8", "1234", "11", "연호8", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호8", "abc");

        userGenerator.requestMatching("fnel1", "1234", "fnel2");
        userGenerator.requestMatching("fnel1", "1234", "fnel3");
        userGenerator.createMatching("fnel1", "1234", "fnel4", "1234");
        userGenerator.createMatching("fnel1", "1234", "fnel5", "1234");


        List<UserMatching> matchingCandidate2 = matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);
        assertThat(matchingCandidate2).extracting("loginId").containsExactly("fnel6", "fnel7", "fnel8");

        List<String> cachedList = redisTemplate.opsForList().range("fnel1" + MATCHING_CANDIDATE_REDIS_KEY, 0, -1);
        assertThat(cachedList).containsExactly("fnel2", "fnel3");

        List<String> cachedList2 = redisTemplate.opsForList().range("fnel1" + MATCHING_LIST_REDIS_KEY, 0, -1);
        assertThat(cachedList2).containsExactly("fnel4", "fnel5");

    }

    @Test
    void test6() {

        Boolean hasKey = redisTemplate.hasKey("fnel1" + MATCHING_CANDIDATE_REDIS_KEY);
        assertThat(hasKey).isFalse();

        Boolean hasKey2 = redisTemplate.hasKey("fnel1" + MATCHING_LIST_REDIS_KEY);
        assertThat(hasKey2).isFalse();

        List<UserMatching> matchingCandidate2 = matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);
        assertThat(matchingCandidate2).extracting("loginId").containsExactly("fnel2", "fnel3", "fnel4");

        List<UserMatching> matchingCandidate3 = matchingFacadeV1.searchMatchingPartner("fnel1", CACHE_DURATION_DAY, CACHE_DURATION_UNIT_DAYS);

        List<String> cachedList = redisTemplate.opsForList().range("fnel1" + MATCHING_CANDIDATE_REDIS_KEY, 0, -1);
        assertThat(cachedList).containsExactly(KEYWORD_DUMMY_CACHE);

        List<String> cachedList2 = redisTemplate.opsForList().range("fnel1" + MATCHING_LIST_REDIS_KEY, 0, -1);
        assertThat(cachedList2).containsExactly(KEYWORD_DUMMY_CACHE);
    }

    //TODO LastId 이후로 매칭 조회+Count관련 로직

}