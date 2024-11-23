package com.plaything.api.domain.repository.repo.log;

import com.plaything.api.domain.repository.entity.log.AdViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdViewLogRepository extends JpaRepository<AdViewLog, Long> {

    List<AdViewLog> findByUser_LoginId(String loginId);
}
