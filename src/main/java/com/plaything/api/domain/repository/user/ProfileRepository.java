package com.plaything.api.domain.repository.user;

import com.plaything.api.domain.repository.entity.user.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
