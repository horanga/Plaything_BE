package com.plaything.api.domain.repository.repo.monitor;

import com.plaything.api.domain.repository.entity.user.UserViolationStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserViolationStatsRepository extends JpaRepository<UserViolationStats, Long> {

}
