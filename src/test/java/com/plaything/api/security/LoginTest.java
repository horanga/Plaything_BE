package com.plaything.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaything.api.domain.auth.client.dto.request.GoogleLoginRequest;
import com.plaything.api.domain.auth.model.request.CreateUserRequest;
import com.plaything.api.domain.auth.model.response.LoginResponse;
import com.plaything.api.domain.auth.service.AuthServiceV1;
import com.plaything.api.domain.key.model.response.AvailablePointKey;
import com.plaything.api.domain.key.service.PointKeyFacadeV1;
import com.plaything.api.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
public class LoginTest {


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthServiceV1 authService;

    @Autowired
    private PointKeyFacadeV1 pointKeyFacadeV1;

    @Autowired
    private RedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {

        Set<String> keys = redisTemplate.keys("*"); // 모든 키 조회
        if (!keys.isEmpty()) {  // null과 빈 set 체크
            redisTemplate.delete(keys);
        }

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())  // Spring Security 설정 적용
                .build();
    }

    @Test
    @DisplayName("로그인 성공시 무료 포인트가 지급된다")
    void test1() throws Exception {
        // given
        userGenerator.generate("fnel123", "123", "dd", "연호");
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest("fnel123", "123");

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .header("Transaction-ID", "ASDAFSFASSSDSs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();  // 결과를 받아옵니다

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("fnel123");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(1L);


        // 포인트 지급 확인
        assertThat(response.dailyRewardProvided()).isTrue();
        assertThat(response.invalidProfile()).isFalse();

    }

    @Test
    @DisplayName("로그인 보상은 하루에 한번만 받을 수 있다.")
    void test2() throws Exception {
        // given
        userGenerator.generate("fnel123", "123", "dd", "연호");
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest("fnel123", "123");

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .header("Transaction-ID", "ASDAFSFASSSDSs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();  // 결과를 받아옵니다

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("fnel123");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(1L);


        // 포인트 지급 확인
        assertThat(response.dailyRewardProvided()).isTrue();
        assertThat(response.invalidProfile()).isFalse();


        // when & then
        MvcResult result2 = mockMvc.perform(post("/api/v1/auth/login")
                        .header("Transaction-ID", "ASDAFSFASSSDSsasd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();  // 결과를 받아옵니다

        LoginResponse response2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(),
                LoginResponse.class
        );

        AvailablePointKey availablePointKey2 = pointKeyFacadeV1.getAvailablePointKey("fnel123");
        assertThat(availablePointKey2.availablePointKey()).isEqualTo(1L);

        // 포인트 지급 확인
        assertThat(response2.dailyRewardProvided()).isFalse();
        assertThat(response2.invalidProfile()).isFalse();
    }

    @Test
    @DisplayName("프로필을 만들지 않으면, 로그인 시 프로필이 유효하지 않다는 응답이 온다")
    void test3() throws Exception {

        authService.creatUser(new CreateUserRequest("fnel123", "123", "dd"));
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest("fnel123", "123");

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .header("Transaction-ID", "ASDAFSFASSSxsDSs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();  // 결과를 받아옵니다

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        AvailablePointKey availablePointKey1 = pointKeyFacadeV1.getAvailablePointKey("fnel123");
        assertThat(availablePointKey1.availablePointKey()).isEqualTo(1L);

        // 포인트 지급 확인
        assertThat(response.dailyRewardProvided()).isTrue();
        assertThat(response.invalidProfile()).isTrue();

    }

    @Test
    @DisplayName("프로필이 있으면 로그인 시 프로필이 유효하다는 응답이 온다")
    void test4() throws Exception {

        userGenerator.generate("fnel123", "123", "dd", "연호");
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest("fnel123", "123");

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .header("Transaction-ID", "ASDAFSFASSSDSs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();  // 결과를 받아옵니다

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        assertThat(response.invalidProfile()).isFalse();

    }
}
