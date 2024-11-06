package com.plaything.api.domain.repository.repo.monitor;

import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserViolationStatsRepository extends JpaRepository<UserViolationStats, Long> {

    Optional<UserViolationStats> findByUser(User user);
}
