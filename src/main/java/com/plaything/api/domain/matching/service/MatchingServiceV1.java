package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.user.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MatchingServiceV1 {

    private final UserServiceV1 userServiceV1;

    public List<UserMatching> match(String user, long lastId){
        return userServiceV1.searchPartner(user, lastId);
        }
}
