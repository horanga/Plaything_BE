package com.plaything.api.domain.matching.service;

import static com.plaything.api.domain.notification.constant.NotificationType.MATCHING_REQUEST;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.chat.service.ChatRoomServiceV1;
import com.plaything.api.domain.key.service.PointKeyServiceV1;
import com.plaything.api.domain.matching.model.request.MatchRequest;
import com.plaything.api.domain.matching.model.request.MatchRequestForOthers;
import com.plaything.api.domain.matching.model.response.MatchingResponse;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.notification.service.NotificationServiceV1;
import com.plaything.api.domain.profile.constants.MatchingRelationship;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.model.response.PersonalityTraitResponse;
import com.plaything.api.domain.profile.model.response.ProfileImageResponse;
import com.plaything.api.domain.profile.model.response.RelationshipPreferenceResponse;
import com.plaything.api.domain.profile.service.UserServiceV1;
import com.plaything.api.domain.profile.util.ImageUrlGenerator;
import com.plaything.api.domain.repository.entity.matching.Matching;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.matching.MatchingRepository;
import com.plaything.api.domain.repository.repo.query.ProfileQueryRepository;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MatchingServiceV1 {

  private final UserServiceV1 userServiceV1;
  private final PointKeyServiceV1 pointKeyServiceV1;
  private final NotificationServiceV1 notificationServiceV1;
  private final ChatRoomServiceV1 chatRoomServiceV1;
  private final ImageUrlGenerator imageUrlGenerator;

  private final MatchingRepository matchingRepository;
  private final ProfileQueryRepository profileQueryRepository;

  public List<UserMatching> searchPartner(String loginId, List<String> matchingCandidates,
      List<String> matchingList, List<String> hideList, long lastId) {
    User userByName = findByLoginIdForRegistration(loginId);
    //TODO 프로필 거절, 사진 거절
    Profile profile = userByName.getProfile();
    validateRequest(profile);

    if (profile.isSwitch() || profile.isETC()) {
      MatchRequestForOthers matchRequest
          = MatchRequestForOthers.from(profile.getPrimaryRole(), lastId, profile.getNickName());
      List<Profile> profiles = profileQueryRepository.searchUserForOthers(matchRequest,
          matchingCandidates, matchingList, hideList);
      return getUserMatchingInfo(profiles);

    }
    PersonalityTraitConstant partnerTrait = MatchingRelationship.getPartner(profile);
    MatchRequest matchRequest =
        MatchRequest.from(
            profile.getPrimaryRole(),
            partnerTrait,
            lastId,
            profile.getNickName());
    List<Profile> profiles = profileQueryRepository.searchUser(matchRequest, matchingCandidates,
        matchingList, hideList);
    return getUserMatchingInfo(profiles);
  }

  @Transactional(rollbackFor = Exception.class)
  public void creatMatching(User requester, User partner, String transactionId) {
    pointKeyServiceV1.usePointKey(requester, partner, transactionId);
    createMatchingLog(requester.getLoginId(), partner.getLoginId());
    try {
      notificationServiceV1.saveNotification(
          MATCHING_REQUEST,
          requester.getLoginId(),
          requester.getProfile(),
          partner);
    } catch (IOException e) {
      throw new CustomException(ErrorCode.NOTIFICATION_SAVED_FAILED);
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void acceptMatching(User matchingReceiver, User matchingSender, String transactionId) {
    pointKeyServiceV1.usePointKey(matchingReceiver, matchingSender, transactionId);
    Matching matching = matchingRepository.findBySenderLoginIdAndReceiverLoginId(
            matchingSender.getLoginId(), matchingReceiver.getLoginId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_MATCHING));
    matching.acceptMatching();
    chatRoomServiceV1.creatChatRoom(matchingSender.getLoginId(), matchingReceiver.getLoginId());
  }

  public void createMatchingLog(String senderLoginId, String receiverLoginId) {
    Matching matching = Matching.builder()
        .senderLoginId(senderLoginId)
        .receiverLoginId(receiverLoginId)
        .build();
    matchingRepository.save(matching);
  }

  @Transactional(readOnly = true)
  public List<MatchingResponse> getMatchingResponse(String loginId) {
    return matchingRepository.findSuccessAndNotOverMatching(loginId).stream()
        .map(MatchingResponse::toResponse).toList();
  }

  public List<String> getMatchingPartner(String loginId) {
    List<Matching> mathingList = matchingRepository.findSuccessAndNotOverMatching(loginId);
    return mathingList.stream().map(matching -> {
      if (matching.getReceiverLoginId().equals(loginId)) {
        return matching.getSenderLoginId();
      } else {
        return matching.getReceiverLoginId();
      }
    }).toList();
  }

  public List<String> getMatchingCandidate(String loginId) {
    List<Matching> matchingCandidate = matchingRepository.findMatchingCandidate(loginId);
    return matchingCandidate.stream().map(matching -> {
      if (matching.getReceiverLoginId().equals(loginId)) {
        return matching.getSenderLoginId();
      } else {
        return matching.getReceiverLoginId();
      }
    }).toList();
  }

  private User findByLoginIdForRegistration(String loginId) {
    return userServiceV1.findByLoginIdForRegistration(loginId);
  }

  private List<UserMatching> getUserMatchingInfo(List<Profile> profiles) {
    return profiles.stream()
        .map(p -> new UserMatching(
            p.getLoginId(),
            p.getPrimaryRoleAsString(),
            p.getNickName(),
            p.getBirthDate(),
            p.getIntroduction(),
            p.getPersonalityTrait().stream()
                .map(
                    trait -> new PersonalityTraitResponse(trait.getTrait(), trait.isPrimaryTrait()))
                .collect(Collectors.toList()),
            p.getRelationshipPreference().stream()
                .map(pref -> new RelationshipPreferenceResponse(pref.getRelationshipPreference()))
                .collect(Collectors.toList()),
            p.getProfileImages().stream()
                .map(prof -> ProfileImageResponse.from(prof,
                    imageUrlGenerator.getImageUrl(prof.getFileName())))
                .collect(Collectors.toList())
        ))
        .collect(Collectors.toList());
  }

  private void validateRequest(Profile profile) {
    if (profile.isProfileImagesEmpty()) {
      throw new CustomException(ErrorCode.MATCHING_FAIL_WITHOUT_IMAGE);
    }

    if (profile.isBaned()) {
      throw new CustomException(ErrorCode.MATCHING_FAIL_WITH_BAN_PROFILE);
    }
  }
}
