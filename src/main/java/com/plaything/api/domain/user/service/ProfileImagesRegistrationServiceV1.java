package com.plaything.api.domain.user.service;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.profile.ProfileImageRegistration;
import com.plaything.api.domain.repository.user.ProfileImagesRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProfileImagesRegistrationServiceV1 {

    private final ProfileImagesRegistrationRepository profileImagesRegistrationRepository;

    @Transactional
    public void saveImageRegistration(ProfileImage profileImage){
        ProfileImageRegistration registration = ProfileImageRegistration.builder()
                .profileImage(profileImage)
                .build();

        profileImagesRegistrationRepository.save(registration);
    }
}
