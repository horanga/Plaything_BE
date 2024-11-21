package com.plaything.api.common.validator;

import com.plaything.api.TestRedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(TestRedisConfig.class)
@Transactional
@SpringBootTest
public class DuplicateCheckerTest {


    @Autowired
    private DuplicateRequestChecker duplicateRequestChecker;

    @Test
    @DisplayName("Redis 정상 동작시 중복 요청 체크")
    void checkDuplicateRequest_Normal() {
        String userId = "testUser";
        String transactionId = "transaction123";

        duplicateRequestChecker.checkDuplicateRequest(userId, transactionId);

        boolean isDuplicate = duplicateRequestChecker.checkDuplicateRequest(userId, transactionId);
        assertThat(isDuplicate).isFalse();
    }
}
