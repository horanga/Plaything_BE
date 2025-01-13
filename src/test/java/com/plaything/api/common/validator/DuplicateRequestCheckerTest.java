package com.plaything.api.common.validator;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.domain.key.model.request.AdRewardRequest;
import com.plaything.api.domain.key.model.request.MatchingRequest;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.domain.matching.service.MatchingFacadeV1;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.pay.PointKeyRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.util.UserGenerator;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;

    @Autowired
    private UserGenerator userGenerator;


    @BeforeEach
    void setUp() {


        Set<String> keys = mockRedis.keys("*"); // 모든 키 조회
        if (keys != null && !keys.isEmpty()) {  // null과 빈 set 체크
            mockRedis.delete(keys);
        }


        userGenerator.generate("dusgh1234", "1234", "1", "연호");
        userGenerator.addImages("연호", "ㅇㅇ", true);

        userGenerator.generate("dusgh12345", "1234", "1", "연호2");
        userGenerator.addImages("연호2", "ㅇㅇ", true);
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
                .isInstanceOf(CustomException.class).hasMessage("이미 처리된 요청입니다");

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
                .isInstanceOf(CustomException.class).hasMessage("이미 처리된 요청입니다");

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

        MatchingRequest matchingRequest = new MatchingRequest("dusgh12345");

        em.flush();
        em.clear();
        matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, transactionId);

        assertThatThrownBy(() -> matchingFacadeV1.sendMatchingRequest("dusgh1234", matchingRequest, transactionId))
                .isInstanceOf(CustomException.class).hasMessage("이미 처리된 요청입니다");

        verify(pointKeyRepository, times(2)).existsByTransactionId(transactionId);
        verify(duplicateRequestChecker, times(2)).fallback(
                eq("dusgh1234"),
                eq(transactionId),
                any(RedisCommandTimeoutException.class)
        );
    }
}