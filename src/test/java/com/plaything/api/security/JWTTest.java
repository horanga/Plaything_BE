package com.plaything.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaything.api.domain.auth.client.google.dto.request.LoginRequest;
import com.plaything.api.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
class JWTTest {

    @Autowired
    private JWTProvider jwtProvider;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserGenerator userGenerator;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())  // Spring Security 설정 적용
                .build();
    }

    @DisplayName("JWT 토큰을 생성하고, 이를 해독해 로그인 ID와 권한을 추출할 수 있다")
    @Test
    void test1() {
        String token = jwtProvider.createToken("fnel123", "ROLE_USER", 60 * 60 * 1000L);


        Boolean isExpired = jwtProvider.isExpired(token);
        String user = jwtProvider.getUsername(token);
        String role = jwtProvider.getRole(token);

        assertThat(isExpired).isFalse();
        assertThat(user).isEqualTo("fnel123");
        assertThat(role).isEqualTo("ROLE_USER");
    }

    @DisplayName("유효기간이 지난 JWT 토큰을 분별할 수 있다")
    @Test
    void test2() throws InterruptedException {
        String token = jwtProvider.createToken("fnel123", "ROLE_USER", 1000L);
        Thread.sleep(2000L);
        Boolean isExpired = jwtProvider.isExpired(token);

        assertThat(isExpired).isTrue();
    }

    @DisplayName("헤더가 없으면 401 응답이 온다")
    @Test
    void test3() throws Exception {
        mockMvc.perform(get("/api/v1/user/get-profile"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("JWT 토큰 헤더가 Bearer로 시작하지 않으면 401 응답이 온다")
    @Test
    void test4() throws Exception {
        mockMvc.perform(get("/api/v1/user/get-profile")
                        .header("Authorization", "Wrong token"))
                .andExpect(status().isUnauthorized());
    }


    @DisplayName("유효기간이 만료된 토큰은 401응답을 받는다.")
    @Test
    void test5() throws Exception {
        // 실제로 만료된 토큰 생성
        String expiredToken = jwtProvider.createToken("testUser", "ROLE_USER", 1000L);

        Thread.sleep(2000L);

        mockMvc.perform(get("/api/v1/user/get-profileg")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("정상적인 JWT 토큰을 사용하면 200응답을 받는다.")
    @Test
    void test6() throws Exception {
        // 실제로 만료된 토큰 생성

        userGenerator.generate("fnel123", "123", "dd", "연호");


        String token = jwtProvider.createToken("fnel123", "ROLE_USER", 60 * 60 * 1000L);
        Thread.sleep(1000L);

        mockMvc.perform(get("/api/v1/user/get-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @DisplayName("로그인 API는 JWT토큰이 필요하지 않다")
    @Test
    void test7() throws Exception {
        // 실제로 만료된 토큰 생성

        userGenerator.generate("fnel123", "123", "dd", "연호");
        LoginRequest loginRequest = new LoginRequest("fnel123", "123");

        System.out.println(objectMapper.writeValueAsString(loginRequest));
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Transaction-ID", "dadgasgdsa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }


    //관리자 권한 api도 추가
}