package com.plaything.api.domain.chat.service;

import com.plaything.api.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@SpringBootTest
class ChatRateLimiterTest {

    @Autowired
    private ChatRateLimiter chatRateLimiter;

    @Test
    void test() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger exceptionCount = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> {
                try {
                    chatRateLimiter.checkRate("testUser");
                } catch (CustomException e) {
                    exceptionCount.incrementAndGet();
                }
            }));
        }

        // 모든 태스크 완료 대기
        for (Future<?> future : futures) {
            future.get();

        }
        executor.shutdown();
        assertEquals(7, exceptionCount.get());
    }

    @DisplayName("유저의 채팅 rate 데이터를 모두 지워버린다")
    @Test
    void test2() throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger exceptionCount = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> {
                try {
                    chatRateLimiter.checkRate("testUser");
                } catch (CustomException e) {
                    exceptionCount.incrementAndGet();
                }
            }));
        }

        // 모든 태스크 완료 대기
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();

        chatRateLimiter.cleanupOldData();

        assertThat(chatRateLimiter.isEmpty()).isTrue();


    }

    @DisplayName("유저의 채팅 rate를 저장한 내역을 지우는 api를 스케쥴링한다")
    @Test
    void test3() {
        // 테스트 데이터 추가
        chatRateLimiter.checkRate("testUser");

        // 스케줄러 실행 대기 및 검증
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(chatRateLimiter.isEmpty()).isTrue();
                });
    }

}