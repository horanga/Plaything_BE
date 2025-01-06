package com.plaything.api.domain.chat.handler;

import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.chat.model.reqeust.ChatRequest;
import com.plaything.api.domain.chat.model.response.ChatProfile;
import com.plaything.api.domain.notification.service.FcmServiceV1;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.profile.ProfileImage;
import com.plaything.api.domain.repository.repo.monitor.ProfileRecordRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileImageRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.plaything.api.domain.profile.constants.Gender.M;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@SpringBootTest
public class MessageBatchHandlerTest {

    @MockBean
    private FcmServiceV1 fcmService;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MessageBatchHandler messageBatchHandler;

    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRecordRepository profileRecordRepository;

    @BeforeEach
    void setUp() {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);

        LocalDate now = LocalDate.now();
        ProfileRegistration profileRegistration = new ProfileRegistration("알렉1", "hi", M, PrimaryRole.TOP, List.of(PersonalityTraitConstant.BOSS), PersonalityTraitConstant.BOSS, List.of(RelationshipPreferenceConstant.DATE_DS), now);

        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");

        Profile profile1 = profileRepository.findByNickName("알렉1");
        ProfileImage image1 = ProfileImage.builder().profile(profile1).fileName("abc").isMainPhoto(true).build();
        profile1.addProfileImages(List.of(image1));

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

    }

    @AfterEach
    void cleanUp() {

        profileRecordRepository.deleteAll();  // profile_record 레포지토리 추가 필요
        // user는 profile을 참조하므로 user 먼저 삭제
        userRepository.deleteAll();
        // 마지막으로 profile 삭제
        profileImageRepository.deleteAll();
        profileRepository.deleteAll();

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }


    @Test
    void test1() throws InterruptedException {

        // given
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(fcmService).sendMessageTo(any(), any(), any(), any());

        // when
        ChatRequest chatRequest = new ChatRequest(0, "dusgh1234", "dusgh12345", "안녕");
        ChatRequest chatRequest2 = new ChatRequest(1, "dusgh1234", "dusgh12345", "반갑다");
        ChatRequest chatRequest3 = new ChatRequest(2, "dusgh1234", "dusgh12345", "잘지내지?");

        messageBatchHandler.queueMessage("dusgh1234", chatRequest, "abd");
        messageBatchHandler.queueMessage("dusgh1234", chatRequest2, "abd");
        messageBatchHandler.queueMessage("dusgh1234", chatRequest3, "abd");

        // then
        boolean await = latch.await(10, TimeUnit.SECONDS);
        assertTrue(await, "비동기 작업이 완료되지 않았습니다");

        verify(fcmService, times(1)).sendMessageTo(
                any(ChatProfile.class),
                eq("새로운 메시지가 도착했습니다"),
                eq("3개의 메시지가 도착했습니다."),
                eq("abd")
        );
    }
}