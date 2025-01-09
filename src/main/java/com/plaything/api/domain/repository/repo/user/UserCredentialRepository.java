package com.plaything.api.domain.repository.repo.user;

import com.plaything.api.domain.repository.entity.user.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialRepository extends JpaRepository<UserCredentials, Long> {
}
