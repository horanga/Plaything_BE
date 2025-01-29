package com.plaything.api.domain.key.model.response;

import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.constant.KeySource;
import com.plaything.api.domain.key.constant.KeyType;
import com.plaything.api.domain.repository.entity.log.KeyLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Key 사용 로그")
public record PointKeyUsageLog(

    @Schema(description = "로그 id")
    Long id,

    @Schema(description = "key 획득 수단(결제/무료)")
    KeyType keyType,

    @Schema(description = "Key 로그 분류")
    KeyLogStatus keyLogStatus,

    @Schema(description = "Key 획득 소스")
    KeySource keySource,

    @Schema(description = "참조하는 keyId")
    long pointKeyId,

    @Schema(description = "id")
    String userLoginId,

    @Schema(description = "키의 유효기간")
    LocalDateTime keyExpirationDate,

    @Schema(description = "로그 생성 시간")
    LocalDateTime createAt,

    @Schema(description = "키 사용 로그")
    KeyUsageLogResponse keyUsageLog

) {

  public static PointKeyUsageLog toResponse(KeyLog keyLog) {
    return new PointKeyUsageLog(
        keyLog.getId(),
        keyLog.getKeyType(),
        keyLog.getKeyLogStatus(),
        keyLog.getKeySource(),
        keyLog.getKey().getId(),
        keyLog.getUser().getLoginId(),
        keyLog.getKeyExpirationDate(),
        keyLog.getCreatedAt(),
        KeyUsageLogResponse.toResponse(keyLog.getKeyUsageLog())
    );
  }
}

