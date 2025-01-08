package com.plaything.api.domain.profile.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.profile.constants.PersonalityTraitConstant;
import com.plaything.api.domain.profile.constants.PrimaryRole;
import com.plaything.api.domain.profile.model.request.ProfileRegistration;
import com.plaything.api.domain.profile.model.request.ProfileUpdate;
import com.plaything.api.domain.profile.model.response.ProfileImageResponse;
import com.plaything.api.domain.profile.model.response.ProfileResponse;
import com.plaything.api.domain.profile.util.ImageUrlGenerator;
import com.plaything.api.domain.repository.entity.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.plaything.api.domain.repository.entity.profile.ProfileHidePreference;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.profile.ProfileHidePreferenceRepository;
import com.plaything.api.domain.repository.repo.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.plaything.api.domain.matching.constants.MatchingConstants.BLOCK_DURATION;

@RequiredArgsConstructor
@Service
public class ProfileQueryServiceV1 {

    private final ProfileRepository profileRepository;
    private final UserServiceV1 userServiceV1;
    private final ImageUrlGenerator imageUrlGenerator;
    private final ProfileHidePreferenceRepository profileHidePreferenceRepository;

    public void validateRegistration(ProfileRegistration registration, User user) {
        if (user.getProfile() != null) {
            throw new CustomException(ErrorCode.PROFILE_ALREADY_EXIST);
        }
        if (registration.primaryRole().equals(PrimaryRole.TOP) || registration.primaryRole().equals(PrimaryRole.BOTTOM)) {
            validateTraits(registration.personalityTraitConstant(), registration.primaryRole());
        }
    }

    public void validateUpdate(ProfileUpdate updateRequest, Profile profile) {
        //변경하려는 닉네임의 중복체크
        if (!profile.getNickName().equals(updateRequest.nickName())) {
            if (profileRepository.existsByNickName(updateRequest.nickName())) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
        }

        if (updateRequest.primaryRole().equals(PrimaryRole.TOP) || updateRequest.primaryRole().equals(PrimaryRole.BOTTOM)) {
            List<PersonalityTraitConstant> traits = profile.getPersonalityTrait().stream().map(PersonalityTrait::getTrait).collect(Collectors.toList());
            traits.addAll(updateRequest.personalityTraitConstant());
            validateTraits(traits, updateRequest.primaryRole());
        }
    }

    public ProfileResponse getProfileByLoginId(String loginId) {
        User user = userServiceV1.findByLoginId(loginId);
        Profile profile = user.getProfile();

        if (profile == null) {
            throw new CustomException(ErrorCode.NOT_EXIST_PROFILE);
        }

        if (profile.isBaned()) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED_PROFILE);
        }

        List<ProfileImageResponse> profileImageResponseList = profile.getProfileImages().stream().map(i -> new ProfileImageResponse(imageUrlGenerator.getImageUrl(i.getFileName()),
                i.isMainPhoto())).toList();

        return ProfileResponse.toResponse(profile, profileImageResponseList);
    }

    public List<String> getHideList(String loginId) {

        //TODO 숨긴 프로필 리스트 나중에 스케쥴링으로 지우기

        List<ProfileHidePreference> list = profileHidePreferenceRepository.findBySettingUser_LoginId(loginId);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate criteria = LocalDate.now().minusDays(BLOCK_DURATION);

        return list.stream().filter(i -> i.getCreateAt().isAfter(criteria))
                .map(i -> i.getTargetUser().getLoginId()).toList();
    }

    private void validateTraits(List<PersonalityTraitConstant> list, PrimaryRole primaryRole) {
        list.forEach(i -> i.validateRoleCompatibility(primaryRole));
    }


}
