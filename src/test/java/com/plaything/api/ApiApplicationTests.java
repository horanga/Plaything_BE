package com.plaything.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestRedisConfig.class)
@SpringBootTest
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
