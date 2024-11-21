package com.plaything.api.common.validator;

import com.plaything.api.TestRedisConfig;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.RelationshipPreferenceConstant;
import com.plaything.api.domain.user.model.request.ProfileRegistration;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import com.plaything.api.domain.user.service.ProfileImageServiceV1;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.plaything.api.domain.user.constants.Gender.M;
import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.HUNTER;
import static com.plaything.api.domain.user.constants.PrimaryRole.TOP;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Import(TestRedisConfig.class)
@Transactional
@SpringBootTest
class DuplicateRequestCheckerTest {

    @SpyBean
    private DuplicateRequestChecker duplicateRequestChecker;

    @SpyBean
    private PointKeyRepository pointKeyRepository;

    @MockBean
    RedisTemplate<String, String> mockRedis;

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileFacadeV1 profileFacadeV1;

    @Autowired
    private ProfileImageServiceV1 profileImageServiceV1;

    @Autowired
    private EntityManager em;


    @BeforeEach
    void setUp() {

        CreateUserRequest request = new CreateUserRequest("dusgh1234", "1234", "1");
        authServiceV1.creatUser(request);

        CreateUserRequest request2 = new CreateUserRequest("dusgh12345", "1234", "1");
        authServiceV1.creatUser(request2);
    }


    @DisplayName("레디스 에러가 발생했을 때 광고 시청 시 fallback 메서드가 작동한다.")
    @Test
    void test2() {
        String transactionId = "tx123";

        // Mock ValueOperations 객체 생성
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(mockRedis.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(
                eq("dusgh1234" + ":" + transactionId),
                eq("success"),
                eq(10L),
                eq(TimeUnit.SECONDS)
        )).thenThrow(new RedisConnectionFailureException("Redis is down"));
        // when & then

        AdRewardRequest adRewardRequest = new AdRewardRequest("dd", 3);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", adRewardRequest, LocalDateTime.now(), transactionId);

        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForAd("dusgh1234", adRewardRequest, LocalDateTime.now(), transactionId))
                .isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");

        verify(pointKeyRepository, times(2)).existsByTransactionId(transactionId);
        verify(duplicateRequestChecker, times(2)).fallback(
                eq("dusgh1234"),
                eq(transactionId),
                any(RedisConnectionFailureException.class)  // CustomException이 아닌 실제 발생하는 예외
        );
    }

    @DisplayName("레디스 에러가 발생했을 때 첫 로그인 시 fallback 메서드가 작동한다.")
    @Test
    void test3() {
        String transactionId = "tx123";

        // Redis 강제로 실패하게 만들기
        when(mockRedis.opsForValue())
                .thenThrow(new RedisCommandTimeoutException("Redis is down"));

        // when & then
        User user = userRepository.findByLoginId("dusgh1234").get();


        pointKeyFacadeV1.createPointKeyForLogin(user, transactionId, LocalDate.now());


        assertThatThrownBy(() -> pointKeyFacadeV1.createPointKeyForLogin(user, transactionId, LocalDate.now()))
                .isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");

        verify(pointKeyRepository, times(2)).existsByTransactionId(transactionId);
        verify(duplicateRequestChecker, times(2)).fallback(
                eq("dusgh1234"),
                eq(transactionId),
                any(RedisCommandTimeoutException.class)
        );
    }

    @DisplayName("레디스 에러가 발생했을 때 포인트 사용시 fallback 메서드가 작동한다.")
    @Test
    void test4() {
        String transactionId = "tx123";

        // Redis 강제로 실패하게 만들기
        when(mockRedis.opsForValue())
                .thenThrow(new RedisCommandTimeoutException("Redis is down"));

        AdRewardRequest request = new AdRewardRequest("광고1", 2);
        pointKeyFacadeV1.createPointKeyForAd("dusgh1234", request, LocalDateTime.now(), "1");

        ProfileRegistration profileRegistration = new ProfileRegistration("연호1", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration, "dusgh1234");
        User user = userRepository.findByLoginId("dusgh1234").get();
        Profile profile = user.getProfile();
        List<SavedImage> savedImages = List.of(new SavedImage("a", "b"));

        profileImageServiceV1.saveImages(savedImages, profile, 0L);

        ProfileRegistration profileRegistration2 = new ProfileRegistration("연호", "안녕", M, TOP, List.of(HUNTER), HUNTER, List.of(RelationshipPreferenceConstant.MARRIAGE_DS),
                LocalDate.now());
        profileFacadeV1.registerProfile(profileRegistration2, "dusgh12345");

        MatchingRequest matchingRequest = new MatchingRequest("연호");

        em.flush();
        em.clear();
        pointKeyFacadeV1.usePointKeyForMatching("dusgh1234", matchingRequest, transactionId);

        assertThatThrownBy(() -> pointKeyFacadeV1.usePointKeyForMatching("dusgh1234", matchingRequest, transactionId))
                .isInstanceOf(CustomException.class).hasMessage("TRANSACTION ALREADY PROCESSED");

        verify(pointKeyRepository, times(2)).existsByTransactionId(transactionId);
        verify(duplicateRequestChecker, times(2)).fallback(
                eq("dusgh1234"),
                eq(transactionId),
                any(RedisCommandTimeoutException.class)
        );
    }
}