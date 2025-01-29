package com.plaything.api.domain.repository.entity.pay;

import com.plaything.api.domain.key.constant.PointStatus;
import com.plaything.api.domain.repository.entity.common.BaseEntity;
import com.plaything.api.domain.repository.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
    @Index(name = "idx_transaction_id", columnList = "transactionId")
})
@Entity
public class PointKey extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

    /*
    변동 내역을 추적하기 위해서 PointStatus로 관리한다.
    레코드를 업데이트나 삭제하지 않고 생성, 사용으로 레코드를 만들고
    group by를 통해서 개수를 구해오는 방식
     */

  @Column(nullable = false)
  @Enumerated(value = EnumType.STRING)
  private PointStatus status;

  @Column(nullable = false)
  private boolean isValidKey;

  private String transactionId;

  @Column(columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime expirationDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}

