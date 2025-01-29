package com.plaything.api.domain.profile.service.profileImage;

import com.plaything.api.domain.repository.repo.monitor.ProfileImagesRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProfileImagesRegistrationServiceV1 {

  private final ProfileImagesRegistrationRepository profileImagesRegistrationRepository;

}
