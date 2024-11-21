package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.profile.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    long countByProfile_Id(Long profileId);

    ProfileImage findByProfile_NickNameAndIsMainPhoto(String nickName, boolean isMainPhoto);
}
