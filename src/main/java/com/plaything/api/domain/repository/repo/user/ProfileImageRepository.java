package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    long countByProfile_Id(Long profileId);
}
