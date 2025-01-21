package com.plaything.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plaything.api.domain.auth.service.LoginSuccessHandler;
import com.plaything.api.security.JWTFilter;
import com.plaything.api.security.JWTProvider;
import com.plaything.api.security.SecurityConstants;
import com.plaything.api.security.exception.CustomAccessDeniedHandler;
import com.plaything.api.security.exception.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final LoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            JWTProvider jwtProvider,
            ObjectMapper objectMapper,
            LoginSuccessHandler loginSuccessHandler) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
        this.loginSuccessHandler = loginSuccessHandler;
    }


    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        //Form 로그인 disable
        http.formLogin(AbstractHttpConfigurer::disable);

        //Http basic 인증방식 disable
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterBefore(
                new JWTFilter(jwtProvider),
                UsernamePasswordAuthenticationFilter.class
        );
        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(SecurityConstants.getAuthWhitelist()).permitAll()
                                .requestMatchers("/admin").hasRole("ADMIN")
                                .requestMatchers("/error").permitAll()
                                .anyRequest().authenticated()
                );


        //세션 설정
        http.sessionManagement((session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)));

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
