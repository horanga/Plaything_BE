package com.plaything.api.domain.auth.service;

import com.plaything.api.domain.auth.client.constants.OAuth2Provider;
import com.plaything.api.domain.auth.model.response.LoginResult;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class AuthServiceV1Test {

    @Autowired
    private AuthServiceV1 authServiceV1;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("구글 로그인 시 회원이 없으면 회원가입후 jwt 토큰을 반환한다")
    @Test
    void test1() {

        LoginResult login = authServiceV1.login(OAuth2Provider.GOOGLE, "123", "DADFAS", "ABC");
        assertThat(login.token()).isNotNull();
        assertThat(login.loginResponse().dailyRewardProvided()).isTrue();
        assertThat(login.loginResponse().invalidProfile()).isTrue();

        User user = userRepository.findByLoginId("GOOGLE123").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getLoginId()).isEqualTo("GOOGLE123");
    }

    @DisplayName("구글 로그인 시 회원이 있으면 로그인을 할 수 있다")
    @Test
    void test2() {

        LoginResult login = authServiceV1.login(OAuth2Provider.GOOGLE, "123", "DADFAS", "ABC");
        LoginResult login2 = authServiceV1.login(OAuth2Provider.GOOGLE, "123", "DADFASAD", "DDD");

        assertThat(login.token()).isNotNull();
        assertThat(login2.loginResponse().dailyRewardProvided()).isFalse();
        User user = userRepository.findByLoginId("GOOGLE123").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getFcmToken()).isEqualTo("DDD");
    }

}