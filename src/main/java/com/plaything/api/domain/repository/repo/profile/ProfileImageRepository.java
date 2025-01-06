package com.plaything.api.domain.repository.repo.profile;

import com.plaything.api.domain.repository.entity.profile.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    long countByProfile_Id(Long profileId);

    ProfileImage findByProfile_NickNameAndIsMainPhoto(String nickName, boolean isMainPhoto);
}
