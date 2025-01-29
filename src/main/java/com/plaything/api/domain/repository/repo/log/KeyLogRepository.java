package com.plaything.api.domain.repository.repo.log;

import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.constant.KeyType;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyLogRepository extends JpaRepository<KeyLog, Long> {

  List<KeyLog> findByUser_LoginIdAndKeyType(String loginId, KeyType keyType);

  List<KeyLog> findByUser_LoginIdAndKeyLogStatus(String loginId, KeyLogStatus status);

}
