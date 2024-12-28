package com.plaything.api.domain.matching.service;

import com.plaything.api.util.UserGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MatchingFacadeV1Test {

    @Autowired
    private MatchingFacadeV1 matchingFacadeV1;
    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

}