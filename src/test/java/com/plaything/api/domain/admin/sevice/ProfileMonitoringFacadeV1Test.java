package com.plaything.api.domain.admin.sevice;

import com.plaything.api.domain.admin.model.response.ProfileRecordResponse;
import com.plaything.api.domain.repository.entity.monitor.RejectedProfile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.monitor.RejectedProfileRepository;
import com.plaything.api.domain.repository.repo.monitor.UserViolationStatsRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import com.plaything.api.domain.user.constants.PrimaryRole;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.constants.Role;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.plaything.api.domain.user.constants.Gender.M;
import static com.plaything.api.domain.user.constants.ProfileStatus.NEW;
import static org.assertj.core.api.Assertions.assertThat;
@Transactional
@SpringBootTest
class ProfileMonitoringFacadeV1Test {

    @Autowired
    private ProfileMonitoringFacadeV1 profileMonitoringFacadeV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RejectedProfileRepository rejectedProfileRepository;

    @Autowired
    private UserViolationStatsRepository userViolationStatsRepository;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {

        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }
        User user = User.builder()
                .loginId("fnel123")
                .role(Role.ROLE_USER)
                .fcmToken("1")
                .build();
        userRepository.save(user);

        User user2 = User.builder()
                .loginId("fnel1234")
                .role(Role.ROLE_USER)
                .fcmToken("1")
                .build();
        userRepository.save(user2);
    }

    @DisplayName("프로필을 등록하면 모니터링 기록이 남는다.")
    @Test
    void test1() {
        ProfileRegistration profileRegistration =
                new ProfileRegistration(
                        "알렉스",
                        "잘부탁드려요!",
                        M,
                        PrimaryRole.BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT),
                        PersonalityTraitConstant.SERVANT,
                        List.of(RelationshipPreferenceConstant.DATE_DS),
                        LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "fnel123");

        ProfileRegistration profileRegistration2 =
                new ProfileRegistration(
                        "알렉스2",
                        "잘부탁드려요!",
                        M,
                        PrimaryRole.BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT),
                        PersonalityTraitConstant.SERVANT,
                        List.of(RelationshipPreferenceConstant.DATE_DS),
                        LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "fnel1234");
        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        assertThat(records).hasSize(2);
        assertThat(records).extracting("nickName").containsExactly("알렉스", "알렉스2");
        assertThat(records).extracting("introduction").containsExactly("잘부탁드려요!", "잘부탁드려요!");
        assertThat(records).extracting("status").containsExactly(NEW, NEW);
    }

    @DisplayName("프로필을 승인하면 레코드가 사라진다.")
    @Test
    void test2() {

        ProfileRegistration profileRegistration =
                new ProfileRegistration(
                        "알렉스",
                        "잘부탁드려요!",
                        M,
                        PrimaryRole.BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT),
                        PersonalityTraitConstant.SERVANT,
                        List.of(RelationshipPreferenceConstant.DATE_DS),
                        LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "fnel123");

        ProfileRegistration profileRegistration2 =
                new ProfileRegistration(
                        "알렉스2",
                        "잘부탁드려요!",
                        M,
                        PrimaryRole.BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT),
                        PersonalityTraitConstant.SERVANT,
                        List.of(RelationshipPreferenceConstant.DATE_DS),
                        LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "fnel1234");

        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        profileMonitoringFacadeV1.approveProfile(records.get(0).recordId());

        List<ProfileRecordResponse> records2 = profileMonitoringFacadeV1.getRecords();

        assertThat(records2).hasSize(1);
        assertThat(records2).extracting("nickName").containsExactly("알렉스2");
        assertThat(records2).extracting("introduction").containsExactly("잘부탁드려요!");
        assertThat(records2).extracting("status").containsExactly(NEW);
    }

    @DisplayName("문제가 있는 프로필을 거절할 수 있다.")
    @Test
    void test3() {
        ProfileRegistration profileRegistration =
                new ProfileRegistration(
                        "알렉스",
                        "잘부탁드려요!",
                        M,
                        PrimaryRole.BOTTOM,
                        List.of(PersonalityTraitConstant.SERVANT),
                        PersonalityTraitConstant.SERVANT,
                        List.of(RelationshipPreferenceConstant.DATE_DS),
                        LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "fnel123");

        List<ProfileRecordResponse> records = profileMonitoringFacadeV1.getRecords();

        profileMonitoringFacadeV1.rejectProfile(records.get(0).recordId(), "수위가 너무 높음");

        List<ProfileRecordResponse> records2 = profileMonitoringFacadeV1.getRecords();

        assertThat(records2).hasSize(0);
        List<RejectedProfile> all = rejectedProfileRepository.findAll();
        User user1 = userRepository.findByLoginId("fnel123").get();

        assertThat(all).hasSize(1);
        assertThat(all).extracting("nickName").containsExactly("알렉스");
        assertThat(all).extracting("introduction").containsExactly("잘부탁드려요!");
        assertThat(all).extracting("rejectedReason").containsExactly("수위가 너무 높음");
        assertThat(all.get(0).getUser().getLoginId()).isEqualTo("fnel123");

        assertThat(user1.isPreviousProfileRejected()).isEqualTo(true);
        assertThat(user1.getProfile().isBaned()).isEqualTo(true);

        //TODO userStats도 확인하기
        UserViolationStats userViolationStats = userViolationStatsRepository.findByUser(user1).get();

        assertThat(userViolationStats).isNotNull();
        assertThat(userViolationStats.getBannedProfileCount()).isEqualTo(1L);
        assertThat(userViolationStats.getBannedImageCount()).isEqualTo(0L);
        assertThat(userViolationStats.getReportViolationCount()).isEqualTo(0L);


    }
}