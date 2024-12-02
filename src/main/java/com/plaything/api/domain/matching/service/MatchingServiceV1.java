package com.plaything.api.domain.matching.service;

import com.plaything.api.domain.matching.model.response.MatchingResponse;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.user.service.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MatchingServiceV1 {

    private final UserServiceV1 userServiceV1;
    private final MatchingRepository matchingRepository;

    public List<UserMatching> match(String user, long lastId) {
        return userServiceV1.searchPartner(user, lastId);
    }

    public void createMatchingLog(String senderNickname, String receiverNickname) {
        Matching matching = Matching.builder().senderNickname(senderNickname).receiverNickname(receiverNickname).build();
        matchingRepository.save(matching);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> getMatchingResponse(String nickname) {
        return matchingRepository.findSuccessAndNotOveMatching(nickname).stream()
                .map(MatchingResponse::toResponse).toList();
    }
}
