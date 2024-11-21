package com.plaything.api.domain.repository.entity.log;

import com.plaything.api.domain.key.constant.KeyLogStatus;
import com.plaything.api.domain.key.constant.KeySource;
import com.plaything.api.domain.key.constant.KeyType;
import com.plaything.api.domain.repository.entity.common.BaseLogEntity;
import com.plaything.api.domain.repository.entity.pay.PointKey;
import com.plaything.api.domain.repository.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class KeyLog extends BaseLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private KeyType keyType;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private KeyLogStatus keyLogStatus;
    
    @Enumerated(value = EnumType.STRING)
    private KeySource keySource;

    @Column
    private LocalDateTime KeyExpirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id", nullable = false)
    private PointKey key;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "key_usage_log_id", referencedColumnName = "id")
    private KeyUsageLog keyUsageLog;

}
