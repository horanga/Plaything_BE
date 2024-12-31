package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.user.ProfileRepository;
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

import static com.plaything.api.domain.matching.constants.MatchingConstants.LAST_PROFILE_ID_REDIS_KEY;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.DEGRADEE;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.SERVANT;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MatchingFacadeV1Test {

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ProfileRepository profileRepository;

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


    @DisplayName("매칭 가능한 리스트를 조회할 수 있다.")
    @Test
    void test1() {
        List<UserMatching> list = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);

        assertThat(list).extracting("loginId").containsExactly("fnel2", "fnel3", "fnel4");
    }

    @DisplayName("매칭 가능한 상대가 없으면 목록에 뜨지 않는다.")
    @Test
    void test2() {
        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, DEGRADEE);
        userGenerator.addImages("연호5", "abc");

        List<Profile> profiles = profileRepository.findByLoginId(List.of("fnel2", "fnel3", "fnel4", "fnel5"));
        matchingFacadeV1.updateLastViewedProfile("fnel5", profiles.get(3).getId(), 1, 1, TimeUnit.HOURS);

        List<UserMatching> list2 = matchingFacadeV1.findMatchingCandidates("fnel5", 1, TimeUnit.HOURS);
        assertThat(list2).isEmpty();
    }

    @DisplayName("기존 매칭정보를 제외하고 매칭 가능 프로필이 뜬다.")
    @Test
    void test3() {
        userGenerator.createMatching("fnel1", "1234", "fnel2", "1234");
        userGenerator.requestMatching("fnel1", "1234", "fnel3");
        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호5", "abc");

        List<UserMatching> list = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);
        assertThat(list).extracting("loginId").containsExactly("fnel4", "fnel5");
    }

    @DisplayName("존재하는 프로필 이상으로 저장된 profileId가 넘어서면, 프로필 처음부터 조회한다")
    @Test
    void test4() {
        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호5", "abc");
        userGenerator.createMatching("fnel1", "1234", "fnel2", "1234");

        List<Profile> profiles = profileRepository.findByLoginId(List.of("fnel2", "fnel3", "fnel4", "fnel5"));

        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(0).getId(), 1, 1, TimeUnit.HOURS);
        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(1).getId(), 1, 1, TimeUnit.HOURS);
        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(2).getId(), 1, 1, TimeUnit.HOURS);
        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(3).getId(), 1, 1, TimeUnit.HOURS);

        List<UserMatching> list = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);
        assertThat(list).extracting("loginId").containsExactly("fnel3", "fnel4", "fnel5");
    }

    @DisplayName("매칭 프로필은 lastProfileId 이후로 조회된다.")
    @Test
    void test5() {
        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호5", "abc");
        userGenerator.createMatching("fnel1", "1234", "fnel2", "1234");

        List<Profile> profiles = profileRepository.findByLoginId(List.of("fnel2", "fnel3", "fnel4", "fnel5"));

        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(0).getId(), 1, 1, TimeUnit.HOURS);
        List<UserMatching> list = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);
        assertThat(list).extracting("loginId").containsExactly("fnel3", "fnel4", "fnel5");
        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        redisTemplate.delete(keys);

        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(1).getId(), 1, 1, TimeUnit.HOURS);
        List<UserMatching> list2 = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);
        assertThat(list2).extracting("loginId").containsExactly("fnel4", "fnel5");
        redisTemplate.delete(keys);

        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(2).getId(), 1, 1, TimeUnit.HOURS);
        List<UserMatching> list3 = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);
        assertThat(list3).extracting("loginId").containsExactly("fnel5");
    }

    @DisplayName("skip count가 50번을 넘어가면 matching candidate중 첫번쨰가 매칭 가능 대상에 포함된다.")
    @Test
    void test6() {

        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호5", "abc");
        userGenerator.requestMatching("fnel1", "1234", "fnel2");
        List<Profile> profiles = profileRepository.findByLoginId(List.of("fnel2", "fnel3", "fnel4", "fnel5"));
        for (int i = 0; i < 51; i++) {
            matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(i % 4).getId(), 1, 1, TimeUnit.HOURS);
        }

        matchingFacadeV1.updateLastViewedProfile("fnel1", profiles.get(3).getId(), 1, 1, TimeUnit.HOURS);
        List<UserMatching> list3 = matchingFacadeV1.findMatchingCandidates("fnel1", 1, TimeUnit.HOURS);
        assertThat(list3).extracting("loginId").containsExactly("fnel2", "fnel3", "fnel4", "fnel5");
    }

    @DisplayName("실전 통합 테스트")
    @Test
    void test7() throws InterruptedException {

        userGenerator.generateWithRole("fnel5", "1234", "11", "연호5", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호5", "abc");
        userGenerator.generateWithRole("fnel6", "1234", "11", "연호6", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호6", "abc");
        userGenerator.generateWithRole("fnel7", "1234", "11", "연호7", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호7", "abc");
        userGenerator.generateWithRole("fnel8", "1234", "11", "연호8", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호8", "abc");
        userGenerator.generateWithRole("fnel9", "1234", "11", "연호9", PrimaryRole.BOTTOM, SERVANT);
        userGenerator.addImages("연호9", "abc");

        for (int i = 10; i < 150; i++) {
            userGenerator.generateWithRole("fnel" + i, "1234", "11", "연호" + i, PrimaryRole.BOTTOM, SERVANT);
            userGenerator.addImages("연호" + i, "abc");
        }

        for (int i = 2; i <= 6; i++) {
            userGenerator.requestMatching("fnel1", "1234", "fnel" + i);
            userGenerator.createMatching("fnel1", "1234", "fnel" + (i + 10), "1234");
        }

        List<UserMatching> list1 = matchingFacadeV1.findMatchingCandidates("fnel1", 2, TimeUnit.SECONDS);
        Thread.sleep(3000);
        assertThat(list1).extracting("loginId").containsExactly(
                "fnel7",
                "fnel8",
                "fnel9",
                "fnel10",
                "fnel11",
                "fnel17",
                "fnel18",
                "fnel19",
                "fnel20",
                "fnel21");


        for (int i = 0; i < 100; i++) {
            matchingFacadeV1.updateLastViewedProfile("fnel1", 0L, 3, 3, TimeUnit.HOURS);
        }

        List<UserMatching> list2 = matchingFacadeV1.findMatchingCandidates("fnel1", 2, TimeUnit.HOURS);

        assertThat(list2).extracting("loginId").containsExactly(
                "fnel2",
                "fnel3",
                "fnel7",
                "fnel8",
                "fnel9",
                "fnel10",
                "fnel11",
                "fnel17",
                "fnel18",
                "fnel19"
        );

        for (long i = 0; i < 1000; i++) {
            matchingFacadeV1.updateLastViewedProfile("fnel1", i, 3, 3, TimeUnit.HOURS);

        }

        List<UserMatching> list3 = matchingFacadeV1.findMatchingCandidates("fnel1", 2, TimeUnit.SECONDS);

        assertThat(list3).extracting("loginId").containsExactly(
                "fnel2",
                "fnel3",
                "fnel4",
                "fnel5",
                "fnel6",
                "fnel7",
                "fnel8",
                "fnel9",
                "fnel10",
                "fnel11"
        );

        matchingFacadeV1.updateLastViewedProfile("fnel1", 0L, 3, 3, TimeUnit.SECONDS);

        String s = redisTemplate.opsForValue().get("fnel1" + LAST_PROFILE_ID_REDIS_KEY);
        assertThat(Long.valueOf(s)).isEqualTo(0L);

        List<Profile> list = profileRepository.findByLoginId(List.of("fnel4"));

        matchingFacadeV1.updateLastViewedProfile("fnel1", list.get(0).getId(), 3, 3, TimeUnit.HOURS);

        Thread.sleep(4000);

        List<UserMatching> list4 = matchingFacadeV1.findMatchingCandidates("fnel1", 2, TimeUnit.SECONDS);

        assertThat(list4).extracting("loginId").containsExactly(
                "fnel5",
                "fnel6",
                "fnel7",
                "fnel8",
                "fnel9",
                "fnel10",
                "fnel11",
                "fnel17",
                "fnel18",
                "fnel19"
        );
    }
}