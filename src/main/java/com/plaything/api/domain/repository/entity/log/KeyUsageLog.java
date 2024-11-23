package com.plaything.api.domain.repository.entity.log;

import com.plaything.api.domain.repository.entity.common.BaseLogEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class KeyUsageLog extends BaseLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long senderId;

    @Column(nullable = false)
    private long receiverId;

}
