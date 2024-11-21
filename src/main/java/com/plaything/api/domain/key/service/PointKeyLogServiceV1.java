package com.plaything.api.domain.key.service;

import com.plaything.api.domain.key.constant.KeySource;
import com.plaything.api.domain.key.constant.KeyType;
import com.plaything.api.domain.key.model.response.PointKeyLog;
import com.plaything.api.domain.key.model.response.PointKeyUsageLog;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import com.plaything.api.domain.repository.entity.log.KeyUsageLog;
import com.plaything.api.domain.repository.entity.pay.PointKey;
import com.plaything.api.domain.repository.entity.user.User;
import com.plaything.api.domain.repository.repo.log.KeyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.plaything.api.domain.key.constant.KeyLogStatus.EARN;
import static com.plaything.api.domain.key.constant.KeyLogStatus.USE;
import static com.plaything.api.domain.key.constant.KeyType.POINT_KEY;

@RequiredArgsConstructor
@Service
public class PointKeyLogServiceV1 {

    private final KeyLogRepository keyLogRepository;

    public List<PointKeyLog> getPointKeyLog(String loginId) {
        return keyLogRepository.findByUser_LoginIdAndKeyType(loginId, KeyType.POINT_KEY)
                .stream()
                .map(PointKeyLog::toResponse)
                .toList();
    }

    public List<PointKeyUsageLog> getPointKeyUsageLog(String loginId) {
        return keyLogRepository.findByUser_LoginIdAndKeyLogStatus(loginId, USE)
                .stream()
                .map(PointKeyUsageLog::toResponse)
                .toList();
    }

    public void createKeyUsageLog(User user, User partner, PointKey usedKey) {
        KeyUsageLog keyUsageLog = KeyUsageLog.builder()
                .senderId(user.getId())
                .receiverId(partner.getId())
                .build();
        KeyLog keyLog = KeyLog.builder().keyLogStatus(USE).key(usedKey).keyType(POINT_KEY).
                user(user).keyUsageLog(keyUsageLog).build();
        keyLogRepository.save(keyLog);
    }

    public void createLog(User user, LocalDateTime expirationDate, PointKey pointKey, KeySource keySource) throws IOException {
        KeyLog keyLog = KeyLog.builder()
                .keyLogStatus(EARN)
                .keyType(POINT_KEY)
                .KeyExpirationDate(expirationDate)
                .keySource(keySource)
                .user(user)
                .key(pointKey)
                .build();
        keyLogRepository.save(keyLog);
    }
}
