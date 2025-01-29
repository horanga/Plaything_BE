package com.plaything.api.domain.repository.repo.profile;

import com.plaything.api.domain.repository.entity.profile.ProfileHidePreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileHidePreferenceRepository extends
    JpaRepository<ProfileHidePreference, Long> {

  List<ProfileHidePreference> findBySettingUser_LoginId(String loginId);
}
