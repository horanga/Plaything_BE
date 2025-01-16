package com.plaything.api.domain.auth.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.auth.model.response.CustomUserDetails;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User userData = userRepository.findByLoginId(username).orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

        //DTO에 넣어서 전달, 이 DTO는 UserDetails를 구현한 DTO로 직접 만들어야 함
        return new CustomUserDetails(userData);
    }
}
