package com.plaything.api.domain.repository.entity.common;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP(0)")
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
