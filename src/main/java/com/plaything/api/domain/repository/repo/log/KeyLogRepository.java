package com.plaything.api.domain.repository.repo.log;

import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.constant.KeyType;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyLogRepository extends JpaRepository<KeyLog, Long> {

    List<KeyLog> findByUser_LoginIdAndKeyType(String loginId, KeyType keyType);

    List<KeyLog> findByUser_LoginIdAndKeyLogStatus(String loginId, KeyLogStatus status);

}
