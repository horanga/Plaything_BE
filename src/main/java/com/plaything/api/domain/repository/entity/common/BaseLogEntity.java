package com.plaything.api.domain.repository.entity.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseLogEntity {

  @CreatedDate
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime modifiedAt;

  @PrePersist
  private void prePersist() {
    createdAt = LocalDateTime.now();
    modifiedAt = null;
  }

  @PreUpdate
  private void preUpdate() {
    modifiedAt = LocalDateTime.now();
  }
}
