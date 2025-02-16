package com.plaything.api.domain.index.service;

import com.plaything.api.domain.index.model.response.Index;
import com.plaything.api.domain.profile.service.ProfileFacadeV1;
import com.plaything.api.domain.repository.repo.query.ChatRoomQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IndexServiceV1 {

  private final ChatRoomQueryRepository chatRoomQueryRepository;
  private final ProfileFacadeV1 profileFacadeV1;

  //TODO 추후 레디스로 변경
  public Index refreshIndex(String loginId) {
    boolean newChat = chatRoomQueryRepository.findNewChat(loginId);
    return new Index(newChat);
  }
}
