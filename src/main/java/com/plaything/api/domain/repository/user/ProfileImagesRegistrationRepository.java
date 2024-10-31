package com.plaything.api.domain.repository.user;

import com.plaything.api.domain.repository.entity.user.profile.ProfileImageRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImagesRegistrationRepository extends JpaRepository<ProfileImageRegistration, Long> {
}
