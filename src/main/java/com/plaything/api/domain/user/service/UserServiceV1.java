package com.plaything.api.domain.user.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.matching.model.request.MatchRequest;
import com.plaything.api.domain.matching.model.request.MatchRequestForOthers;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.repo.monitor.UserViolationStatsRepository;
import com.plaything.api.domain.repository.repo.query.UserQueryRepository;
import com.plaything.api.domain.repository.repo.user.UserRepository;
import com.plaything.api.domain.user.constants.MatchingRelationship;
import com.plaything.api.domain.user.constants.PersonalityTraitConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceV1 {

    private final UserRepository userRepository;
    private final UserViolationStatsRepository userViolationStatsRepository;
    private final UserQueryRepository userQueryRepository;

    public List<UserMatching> searchPartner(String loginId, long lastId) {
        User userByName = findByLoginId(loginId);
        //TODO 프로필 거절, 사진 거절

        Profile profile = userByName.getProfile();

        validateRequest(profile);

        if (profile.isSwitch() || profile.isETC()) {
            MatchRequestForOthers matchRequest
                    = MatchRequestForOthers.from(profile.getPrimaryRole(), lastId, profile.getNickName());
            return userQueryRepository.searchUser(matchRequest);

        }

        PersonalityTraitConstant partnerTrait = MatchingRelationship.getPartner(profile);
        MatchRequest matchRequest =
                MatchRequest.from(
                        profile.getPrimaryRole(),
                        partnerTrait,
                        lastId,
                        profile.getNickName());
        return userQueryRepository.searchUser(matchRequest);
    }

    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));
    }

    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));
    }

    @Transactional
    public void delete(String auth) {
        User user = userRepository.findByLoginId(auth).get();
        user.delete();
    }

    @Transactional
    public void increaseBannedProfileCount(User user) {
        Optional<UserViolationStats> violationStats = userViolationStatsRepository.findByUser(user);
        if (violationStats.isEmpty()) {
            UserViolationStats userViolationStats = UserViolationStats.builder().user(user)
                    .bannedImageCount(0L)
                    .bannedProfileCount(1L)
                    .reportViolationCount(0L)
                    .user(user)
                    .build();
            userViolationStatsRepository.save(userViolationStats);
        } else {
            violationStats.get().increaseBannedProfileCount();
        }
    }

    public User findByProfileNickname(String nickName) {
        return userRepository.findByProfile_nickName(nickName);
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
