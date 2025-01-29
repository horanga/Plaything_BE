package com.plaything.api.domain.admin.service;

import com.plaything.api.domain.admin.model.response.ProfileRecord;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import com.plaything.api.domain.repository.entity.monitor.RejectedProfile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.repo.monitor.RejectedProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.plaything.api.domain.profile.constants.Gender.M;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.*;
import static com.plaything.api.domain.profile.constants.PrimaryRole.TOP;
import static com.plaything.api.domain.profile.constants.ProfileStatus.NEW;
import static com.plaything.api.domain.profile.constants.ProfileStatus.UPDATED;
import static com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class ProfileMonitoringFacadeV1Test {

    @Autowired
    private ProfileMonitoringFacadeV1 profileMonitoringFacadeV1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RejectedProfileRepository rejectedProfileRepository;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @BeforeEach
    void setUp() {
        userGenerator.generate("fnel123", "1234", "12", "알렉스");
        userGenerator.generate("fnel1234", "1234", "12", "알렉스2");
    }

    @DisplayName("프로필을 등록하면 모니터링 기록이 남는다.")
    @Test
    void test1() {

        List<ProfileRecord> records = profileMonitoringFacadeV1.getRecords();

        assertThat(records).hasSize(2);
        assertThat(records).extracting("nickName").containsExactly("알렉스", "알렉스2");
        assertThat(records).extracting("introduction").containsExactly("hi", "hi");
        assertThat(records).extracting("status").containsExactly(NEW, NEW);
    }

    @DisplayName("프로필을 승인하면 레코드가 사라진다.")
    @Test
    void test2() {

        List<ProfileRecord> records = profileMonitoringFacadeV1.getRecords();

        profileMonitoringFacadeV1.approveProfile(records.get(0).recordId());
        List<ProfileRecord> records2 = profileMonitoringFacadeV1.getRecords();

        assertThat(records2).hasSize(1);
        assertThat(records2).extracting("nickName").containsExactly("알렉스2");
        assertThat(records2).extracting("introduction").containsExactly("hi");
        assertThat(records2).extracting("status").containsExactly(NEW);
    }

    @DisplayName("문제가 있는 프로필을 거절할 수 있다.")
    @Test
    void test3() {

        List<ProfileRecord> records = profileMonitoringFacadeV1.getRecords();

        profileMonitoringFacadeV1.rejectProfile(records.get(0).recordId(), "수위가 너무 높음");

        List<ProfileRecord> records2 = profileMonitoringFacadeV1.getRecords();

        assertThat(records2).hasSize(1);
        List<RejectedProfile> all = rejectedProfileRepository.findAll();
        User user1 = userRepository.findByLoginId("fnel123").get();

        assertThat(all).hasSize(1);
        assertThat(all).extracting("nickName").containsExactly("알렉스");
        assertThat(all).extracting("introduction").containsExactly("hi");
        assertThat(all).extracting("rejectedReason").containsExactly("수위가 너무 높음");
        assertThat(all.get(0).getUser().getLoginId()).isEqualTo("fnel123");

        assertThat(user1.isPreviousProfileRejected()).isEqualTo(true);
        assertThat(user1.getProfile().isBaned()).isEqualTo(true);

        //TODO userStats도 확인하기
        UserViolationStats userViolationStats = user1.getViolationStats();

        assertThat(userViolationStats).isNotNull();
        assertThat(userViolationStats.getBannedProfileCount()).isEqualTo(1L);
        assertThat(userViolationStats.getBannedImageCount()).isEqualTo(0L);
        assertThat(userViolationStats.getReportViolationCount()).isEqualTo(0L);


    }

    @DisplayName("프로필을 업데이트하면 모니터링 기록으로 남는다.")
    @Test
    void test4() {

        CreateUserRequest request = new CreateUserRequest("dusgh123", "1234", "aa");
        authServiceV1.createUser(request);

        ProfileRegistration registration = new ProfileRegistration(
                "알렉1",
                "안녕하세요",
                M,
                TOP,
                List.of(SPANKER, HUNTER),
                HUNTER,
                List.of(MARRIAGE_DS,
                        DATE_DS,
                        DS,
                        FWB,
                        PLAYPARTNER,
                        HOM),
                LocalDate.of(1995, 3, 30));

        profileFacadeV1.registerProfile(registration, "dusgh123");

        ProfileUpdate profileUpdate = new ProfileUpdate(
                "알렉2",
                M,
                "hi",
                TOP,
                null,
                List.of(BRAT_TAMER, DOMINANT),
                null,
                null,
                Collections.emptyList()
        );

        profileFacadeV1.updateProfile(profileUpdate, "dusgh123");

        List<ProfileRecord> records = profileMonitoringFacadeV1.getRecords();
        assertThat(records).hasSize(4);
        assertThat(records).extracting("nickName").containsExactly("알렉스", "알렉스2", "알렉1", "알렉2");
        assertThat(records).extracting("introduction").containsExactly("hi", "hi", "안녕하세요", "hi");
        assertThat(records).extracting("status").containsExactly(NEW, NEW, NEW, UPDATED);
    }
}