package com.plaything.api.domain.repository.repo.profile;

import com.plaything.api.domain.repository.entity.profile.ProfileHidePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileHidePreferenceRepository extends JpaRepository<ProfileHidePreference, Long> {

    List<ProfileHidePreference> findBySettingUser_LoginId(String loginId);
}
