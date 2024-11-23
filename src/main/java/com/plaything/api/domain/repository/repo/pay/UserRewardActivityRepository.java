package com.plaything.api.domain.repository.repo.pay;

import com.plaything.api.domain.repository.entity.pay.UserRewardActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRewardActivityRepository extends JpaRepository<UserRewardActivity, Long> {

    Optional<UserRewardActivity> findByUser_loginId(String loginId);
}
