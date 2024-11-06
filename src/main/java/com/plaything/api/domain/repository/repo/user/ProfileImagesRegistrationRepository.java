package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.monitor.ProfileImageRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImagesRegistrationRepository extends JpaRepository<ProfileImageRegistration, Long> {
}
