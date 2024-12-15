package com.plaything.api.domain.index.service;

import com.plaything.api.domain.index.model.response.IndexResponse;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.query.ChatRoomQueryRepository;
import com.plaything.api.domain.user.service.ProfileFacadeV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IndexServiceV1 {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ProfileFacadeV1 profileFacadeV1;

    //TODO 추후 레디스로 변경
    public IndexResponse refreshIndex(String loginId) {
        Profile profile = profileFacadeV1.getProfileByUserLoginId(loginId);
        boolean newChat = chatRoomQueryRepository.findNewChat(profile.getNickName());
        return new IndexResponse(newChat);
    }
}
