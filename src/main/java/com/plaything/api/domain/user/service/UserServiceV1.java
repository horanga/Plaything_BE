package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.user.UserRepository;
import com.plaything.api.domain.repository.entity.user.User;
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

    public User findByName(String name){
       return userRepository.findByName(name)
               .orElseThrow(()->new CustomException(ErrorCode.NOT_EXIST_USER));
    }
}
