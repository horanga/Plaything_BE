package com.plaything.api.domain.repository.repo.log;

import com.plaything.api.domain.repository.entity.log.AdViewLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdViewLogRepository extends JpaRepository<AdViewLog, Long> {

  List<AdViewLog> findByUser_LoginId(String loginId);
}
