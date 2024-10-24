package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.UserRepository;
import com.plaything.api.domain.user.model.response.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceV1 {

    private final UserRepository userRepository;

    public UserSearchResponse searchUser(String name, String user){
        List<String> nameByNameMatch = userRepository.findNameByNameMatch(name, user);

        return new UserSearchResponse(ErrorCode.SUCCESS, nameByNameMatch);

    }



}
